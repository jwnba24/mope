package jwnba24Test;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Created by Administrator on 2018/3/8.
 */
public class Dao {

    /**
     * 批量插入元素
     *
     * @param sqls
     */
    public static void insertBatch(List<String> sqls) throws SQLException {
        Connection con = DbUtil.getCon();
        con.setAutoCommit(false);
        Statement statement = con.createStatement();
        for (String sql : sqls) {
            statement.addBatch(sql);
        }
        statement.executeBatch();
        con.commit();
        DbUtil.closeCon(con);
        System.out.println("执行完成");
    }

    public static void main(String[] args) {
//        final  s;
//        System.out.println(s);
    }

    public static Student selectLast() throws SQLException {
        Student student=null;
        Connection con=DbUtil.getCon();
        Statement statement=con.createStatement();
        ResultSet rs=statement.executeQuery("select * from students order by id desc limit 1");
        if(rs.next()){
            student=new Student();
            student.setPath(rs.getString("path"));
            student.setEnc_score(rs.getString("enc_score"));
//            System.out.println("表中不存在");
//            System.out.println(rs.getString("path"));
//            System.out.println(rs.getString("enc_score"));
        }
        DbUtil.closeCon(con);
        return student;
    }
    public static Student select(double num)throws Exception{
        Student student=null;
        Connection con=DbUtil.getCon();
        Statement statement=con.createStatement();
        String sql="select * from students where enc_score='"+"enc_"+Double.toString(num)+" limit 1'";
        ResultSet rs=statement.executeQuery(sql);
        if(rs.next()){
//            System.out.println("表中已经存在");
//            System.out.println(rs.getString("path"));
//            System.out.println(rs.getInt("height"));
            student.setPath(rs.getString("path"));
            student.setHeight(rs.getInt("height"));
        }
        DbUtil.closeCon(con);
        return student;
    }

    /**
     * 根据路径找数据
     * @param path
     * @return
     */
    public static Student selectByPath(String path) throws Exception{
        Student student=null;
        Connection con=DbUtil.getCon();
        Statement statement=con.createStatement();
        String sql="select * from students where path='"+path+"'";
        ResultSet rs=statement.executeQuery(sql);
        if(rs.next()){
//            System.out.println("表中已经存在");
//            System.out.println(rs.getString("path"));
//            System.out.println(rs.getInt("height"));
            student.setPath(rs.getString("path"));
            student.setHeight(rs.getInt("height"));
            student.setEnc_score(rs.getString("enc_score"));
            student.setEnc_name(rs.getString("enc_name"));
            student.setId(rs.getInt("id"));
        }
        DbUtil.closeCon(con);
        return student;
    }
}
