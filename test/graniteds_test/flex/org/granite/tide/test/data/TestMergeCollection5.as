package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Person0;
    
    
    public class TestMergeCollection5 extends TestCase
    {
        public function TestMergeCollection5() {
            super("testMergeCollection5");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        private var events:Object = { add: 0, remove: 0, update: 0, replace: 0, move: 0, refresh: 0, reset: 0 };
        
        public function testMergeCollection5():void {
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(new Person0(1, "A1", "B1"));
        	coll.addItem(new Person0(2, "A2", "B2"));
        	coll.addItem(new Person0(3, "A3", "B3"));
        	coll = _ctx.meta_mergeExternalData(coll) as ArrayCollection;
        	
        	coll.addEventListener(CollectionEvent.COLLECTION_CHANGE, collectionChangeHandler, false, 0, true);
        	
        	var coll2:ArrayCollection = new ArrayCollection();
        	coll2.addItem(new Person0(4, "A4", "B4"));
        	coll2.addItem(new Person0(1, "A1", "B1"));
        	coll2.addItem(new Person0(2, "A2", "B2"));
        	coll2.addItem(new Person0(3, "A3", "B3"));
        	_ctx.meta_mergeExternalData(coll2, coll);
        	
        	assertEquals("Element 1", 4, coll.getItemAt(0).id);
        	assertEquals("Element 2", 1, coll.getItemAt(1).id);
        	assertEquals("Element 3", 2, coll.getItemAt(2).id);
        	assertEquals("Element 4", 3, coll.getItemAt(3).id);
        	
        	assertEquals("Event add count", 1, events.add); 
        	assertEquals("Event remove count", 0, events.remove); 
        }
        
        private function collectionChangeHandler(event:CollectionEvent):void {
        	events[event.kind]++;
        }
    }
}
