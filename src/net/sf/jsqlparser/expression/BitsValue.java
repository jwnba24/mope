package net.sf.jsqlparser.expression;

/**
 * A bits as in b'00011100'
 */
public class BitsValue implements Expression {
	private String value="";
	
	public BitsValue(String bitsString){
		this.value=bitsString;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String string) {
		value = string;
	}
	public String getStringValue() {
		return value;
	}
	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		return "b'" + value + "'";
	}
}
