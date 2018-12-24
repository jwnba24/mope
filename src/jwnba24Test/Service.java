package jwnba24Test;

/**
 * Created by Administrator on 2018/3/9.
 */
public class Service {
    /**
     * 单条插入数据值
     * @param num
     */
    public static void insert(double num) throws Exception{
        //首先从数据库中查找最新的树的根节点，然后遍历该树找到该值应该的插入位置
        Student s=null;
        s=Dao.select(num);
        if(s!=null){
            System.out.println("该数据在表中已经存在");
        }else{
            s=Dao.selectLast();
            double parent=Double.parseDouble(s.getEnc_score().substring(4));

            Student c_student=s;
            String c_path=c_student.getPath();
            String c_score=c_student.getEnc_score();
            while(c_student!=null){
                c_path=findPath(c_path,c_score,num);
                c_student=Dao.selectByPath(c_path);
            }
            System.out.println(c_path);

        }

    }

    private static String findPath(String c_path, String c_score,double num) throws Exception{
        double score=Double.parseDouble(c_score.substring(4));
        String path="";
        if(num<score){
            path=c_path+"0";
        }else if(num>score){
            path=c_path+"1";
        }else {
            System.out.println("找到相等元素");
        }

        return path;
    }

    public static void main(String[] args) throws Exception {
        insert(122);
    }
}
