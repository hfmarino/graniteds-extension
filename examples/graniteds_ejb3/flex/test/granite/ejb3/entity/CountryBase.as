/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR. INSTEAD, EDIT THE INHERITED CLASS (Country.as).
 */

package test.granite.ejb3.entity {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import org.granite.meta;

    use namespace meta;

    [Bindable]
    public class CountryBase extends AbstractEntity {

        private var _name:String;

        public function set name(value:String):void {
            _name = value;
        }
        public function get name():String {
            return _name;
        }

        public override function readExternal(input:IDataInput):void {
            super.readExternal(input);
            if (meta::isInitialized()) {
                _name = input.readObject() as String;
            }
        }

        public override function writeExternal(output:IDataOutput):void {
            super.writeExternal(output);
            if (meta::isInitialized()) {
                output.writeObject(_name);
            }
        }
    }
}