package ssdb.core.mope;

/**
 * 存储ope Tree中某个节点的mope index与height
 * @author zenggo
 *
 */
public class mOPENodeInfo{
	private String value;
	private int height;
	private String index;
	private String path;
	
	public mOPENodeInfo(String value,int height,String index,String path){
		this.value=value;
		this.height=height;
		this.index=index;
		this.path=path;
	}
	public int getHeight(){
		return height;
	}
	public String getIndex(){
		return index;
	}
	public String getValue(){
		return value;
	}
	public String getPath(){
		return path;
	}
}