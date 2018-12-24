package jwnba24Test;

import java.util.Scanner;

public class CreateDemo {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        String inputSQL = sc.nextLine();
        String outputSQL = "";
        CreateReconstructor createReconstructor = new CreateReconstructor();
        outputSQL = createReconstructor.createTableReconstruct(inputSQL);
        System.out.println(outputSQL);
    }
}
