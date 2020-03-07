import java.io.File;

public class Main {

    public static void main(String[] args) {
        int[] key = {255, 0, 124, 16, 185, 200, 93, 142, 254, 3, 101, 86, 205, 30, 224, 168, 200, 93, 142, 53};
        int fileSize = (int)new File("C:/Users/123/IdeaProjects/krypt2/src/truck_first.bmp").length();

        Encrypter test1 = new Encrypter("C:/Users/123/IdeaProjects/krypt2/src/truck_first.bmp", key, fileSize);
        test1.encryption();

        System.out.println("СORRELATION COEFFICIENT: " + test1.correlation());
        System.out.println("DISTRIBUTION OF '1': " + test1.distribution());
        System.out.println("DISTRIBUTION OF '0': " + (1 - test1.distribution()));

        Decrypter test2 = new Decrypter("C:/Users/123/IdeaProjects/krypt2/src/enc.bmp", key, fileSize);
        test2.decryption();

        System.out.println("DONE.");
    }
}
