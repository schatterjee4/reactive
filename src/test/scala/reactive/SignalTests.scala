package reactive

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class SignalTests extends FunSuite with ShouldMatchers with CollectEvents {
  implicit val observing = new Observing {}
  test("map") {
    val s = Var(10)
    val mapped = s.map[Int, Signal[Int]](_ * 3)
    
    mapped.now should equal (30)
    
    collecting(mapped.change) {
      s ()= 60
      mapped.now should equal (180)
    } should equal (List(180))
  }
  
  test("flatMap (regular T=>Signal[U])") {
    val aVar = Var(3)
    val vals = List(Val(1), Val(2), aVar)
    val parent = Var(0)
    val flatMapped = parent.flatMap(vals)

    collecting(flatMapped.change) {
      flatMapped.now should equal (1)
      
      parent ()= 1
      flatMapped.now should equal (2)
      
      parent ()= 2
      flatMapped.now should equal (3)
      aVar () = 4
      flatMapped.now should equal (4)
      
      parent ()= 0
      flatMapped.now should equal (1)
    
    } should equal (List(2,3,4,1))
  }
  
  test("flatMap (T=>SeqSignal[U])") {
    val bufSig1 = BufferSignal(1,2,3)
    val bufSig2 = BufferSignal(2,3,4)
    val parent = Var(false)
    val flatMapped: SeqSignal[Int] = parent.flatMap {b: Boolean =>
      if(!b) bufSig1 else bufSig2
    }
    
    flatMapped.now should equal (Seq(1,2,3))
    
    collecting(flatMapped.deltas){
      
      collecting(flatMapped.change){
        parent ()= true
        flatMapped.now should equal (Seq(2,3,4))
      } should equal (List(List(2,3,4)))
      
      collecting(flatMapped.change){
        bufSig2 ()= Seq(2,3,4,5)
        flatMapped.now should equal (Seq(2,3,4,5))
      } should equal (List(List(2,3,4,5)))
      
      collecting(flatMapped.change){
        parent ()= false
        flatMapped.now should equal (Seq(1,2,3))    
      } should equal (List(List(1,2,3)))
    
    } should equal (List(
      Batch(Remove(2,3), Remove(1,2), Remove(0,1), Include(0,2), Include(1,3), Include(2,4)),
      Batch(Include(3,5)),
      Batch(Remove(2,4), Remove(1,3), Remove(0,2), Include(0,1), Include(1,2), Include(2,3))
    ))
    
  }
}