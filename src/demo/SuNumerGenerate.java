package demo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/7/15.
 */
public class SuNumerGenerate {
    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        SuNumerGenerate.encrypt(new BigInteger("2"));
        long end = System.currentTimeMillis();
        System.out.println("花费时间："+(end-start));
    }

    private static BigInteger GenerateP(){
        BigInteger p = BigInteger.probablePrime(50,new Random());
        return p;
    }
    //q是个整数并且远大于p
    private static  BigInteger GenerateQ(BigInteger p){
        BigInteger q = new BigInteger(20,new Random());
        return q;
    }
    //q是个整数并且远大于p
    private static  BigInteger GenerateX(BigInteger p){
        BigInteger r = new BigInteger(20,new Random());
        BigInteger q = SuNumerGenerate.GenerateQ(p);
        BigInteger[] bigIntegers = (p.multiply(q).add(r)).divideAndRemainder(new BigInteger("4"));
        while(!bigIntegers[1].equals(new BigInteger("0"))) {
            r = new BigInteger(50,new Random());
            bigIntegers = (p.multiply(q).add(r)).divideAndRemainder(new BigInteger("4"));
        }
        return r;
    }

    private static BigInteger GenerateR(){
        return new BigInteger(20,new Random());
    }

    public static void encrypt(BigInteger m){
        System.out.println("未加密："+m);
        List<BigInteger> list = new ArrayList<>();
        BigInteger p = SuNumerGenerate.GenerateP();
        BigInteger q = SuNumerGenerate.GenerateQ(p);
        BigInteger r = SuNumerGenerate.GenerateR();
        BigInteger result = new BigInteger("0");
        for(int i = 0;i<10;i++){
            BigInteger x = SuNumerGenerate.GenerateX(p);
            result = result.add(x);
        }
        result = result.multiply(q).multiply(new BigInteger("4"));
        r = r.multiply(new BigInteger("4"));
        result = new BigInteger(m+"").add(r).add(result);


    }
}
