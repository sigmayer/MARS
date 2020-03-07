import java.io.*;
import java.util.ArrayList;

public class Encrypter {

    public int[] openText;
    private int[] encryptedText;
    private ArrayList<int[]> key;

    public Encrypter(String fileName, int[] k, int fileSize) {
        openText = readBytes(fileName, fileSize);
        key = Tools.makeKey(k);
    }

    public int[] encryption() {

        int[] block = new int[16];
        ArrayList<int[]> blocks= new ArrayList<>();

        for (int i = 0; i < openText.length; i++) {
            if ((i % 16 == 0) & (i != 0)) {
                blocks.add(block);
                block = new int[16];
            }
            block[i % 16] = openText[i];
        }
        blocks.add(block);

        for (int i = 0; i < blocks.size(); i++) {
            int[] encryptedBlock = directMixing(blocks.get(i), key);
            blocks.set(i, encryptedBlock);
        }
        for (int i = 0; i < blocks.size(); i++) {
            int[] encryptedBlock = encryptCore(blocks.get(i), key);
            blocks.set(i, encryptedBlock);
        }
        for (int i = 0; i < blocks.size(); i++) {
            int[] encryptedBlock = reverseMixing(blocks.get(i), key);
            blocks.set(i, encryptedBlock);
        }

        int count = 0;
        encryptedText = new int[(blocks.size()+1)*16];
        for (int i = 0; i < blocks.size(); i++) {
            for (int j = 0; j < blocks.get(i).length; j++) {
                encryptedText[count] = blocks.get(i)[j];
                count++;
            }
        }
        int res[] = new int[4];
        int num = openText.length;
        for (int i = 3; i >= 0; i--) {
            res[i] = num & 0b11111111;
            num = num >>> 8;
        }
        int tmp = 0;
        for (int i = encryptedText.length-1; i >= encryptedText.length-4; i--) {
            encryptedText[i] = res[tmp];
            tmp++;
        }
        writeBytes("C:/Users/123/IdeaProjects/krypt2/src/enc.bmp");
        return encryptedText;
    }

    public int[] readBytes(String fileName, int fileSize) {
        int[] res;
        try {
            File file = new File(fileName);
            res = new int[fileSize];
            FileInputStream reader = new FileInputStream(file);
            for (int i = 0; i < file.length(); i++) {
                res[i] = reader.read();
            }
            return res;
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void writeBytes(String fileName) {
        try {
            File file = new File(fileName);
            FileOutputStream writer = new FileOutputStream(file);
            BufferedOutputStream stream = new BufferedOutputStream(writer);
            byte[] res = new byte[encryptedText.length];
            for (int i = 0; i < encryptedText.length; i++) {
                res[i] = (byte)(encryptedText[i] & 0xff);
            }
            stream.write(res);
            stream.flush();
            stream.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private int[] directMixing(int[] block, ArrayList<int[]> key) {
        int[] res = new int[block.length];

        int[] D0 = new int[4];
        int[] D1 = new int[4];
        int[] D2 = new int[4];
        int[] D3 = new int[4];

        int[] K0 = key.get(0);
        int[] K1 = key.get(1);
        int[] K2 = key.get(2);
        int[] K3 = key.get(3);

        for (int i = 0; i < block.length; i++) {
            if (0 <= i & i < 4) {
                D0[i] = block[i];
            }
            if (4 <= i & i < 8) {
                D1[i % 4] = block[i];
            }
            if (8 <= i & i < 12) {
                D2[i % 4] = block[i];
            }
            if (12 <= i & i < 16) {
                D3[i % 4] = block[i];
            }
        }

        D0 = Tools.modSum(D0, K0);
        D1 = Tools.modSum(D1, K1);
        D2 = Tools.modSum(D2, K2);
        D3 = Tools.modSum(D3, K3);

        for (int l = 0; l < 8; l++) {
            for (int j = 0; j < 2; j++) {

                //1 ФАЗА
                D1 = Tools.xor(D1, Tools.S0(D0[3]));
                D3 = Tools.xor(D3, Tools.S1(D0[0]));
                D1 = Tools.modSum(D1, Tools.S1(D0[2]));
                D2 = Tools.modSum(D2, Tools.S0(D0[1]));

                D0 = Tools.CSP(D0, 24);

                D0 = Tools.modSum(D0, D3);

                //2 ФАЗА
                D2 = Tools.xor(D2, Tools.S0(D1[3]));
                D0 = Tools.xor(D0, Tools.S1(D1[0]));
                D2 = Tools.modSum(D2, Tools.S1(D1[2]));
                D3 = Tools.modSum(D3, Tools.S0(D1[1]));

                D1 = Tools.CSP(D1, 24);

                D1 = Tools.modSum(D1, D2);

                //3 ФАЗА
                D3 = Tools.xor(D3, Tools.S0(D2[3]));
                D1 = Tools.xor(D1, Tools.S1(D2[0]));
                D3 = Tools.modSum(D3, Tools.S1(D2[2]));
                D0 = Tools.modSum(D0, Tools.S0(D2[1]));

                D2 = Tools.CSP(D2, 24);

                //4 ФАЗА
                D0 = Tools.xor(D0, Tools.S0(D3[3]));
                D2 = Tools.xor(D2, Tools.S1(D3[0]));
                D0 = Tools.modSum(D0, Tools.S1(D3[2]));
                D1 = Tools.modSum(D1, Tools.S0(D3[1]));

                D3 = Tools.CSP(D3, 24);
            }
        }

        for (int i = 0; i < res.length; i++) {
            if (0 <= i & i < 4) {
                res[i] = D0[i];
            }
            if (4 <= i & i < 8) {
                res[i] = D1[i % 4];
            }
            if (8 <= i & i < 12) {
                res[i] = D2[i % 4];
            }
            if (12 <= i & i < 16) {
                res[i] = D3[i % 4];
            }
        }

        return res;
    }

    private int[] encryptCore(int[] block, ArrayList<int[]> key) {

        int[] D0 = new int[4];
        int[] D1 = new int[4];
        int[] D2 = new int[4];
        int[] D3 = new int[4];

        int keyNum = 4;
        for (int l = 0; l < 8; l++) {

            for (int i = 0; i < block.length; i++) {
                if (0 <= i & i < 4) {
                    D0[i] = block[i];
                }
                if (4 <= i & i < 8) {
                    D1[i % 4] = block[i];
                }
                if (8 <= i & i < 12) {
                    D2[i % 4] = block[i];
                }
                if (12 <= i & i < 16) {
                    D3[i % 4] = block[i];
                }
            }
            block = directKeyMixing(D0, D1, D2, D3, key.get(keyNum), key.get(keyNum+1));
            keyNum+=2;
        }

        for (int l = 0; l < 8; l++) {

            for (int i = 0; i < block.length; i++) {
                if (0 <= i & i < 4) {
                    D0[i] = block[i];
                }
                if (4 <= i & i < 8) {
                    D1[i % 4] = block[i];
                }
                if (8 <= i & i < 12) {
                    D2[i % 4] = block[i];
                }
                if (12 <= i & i < 16) {
                    D3[i % 4] = block[i];
                }
            }
            block = reverseKeyMixing(D0, D1, D2, D3, key.get(keyNum), key.get(keyNum+1));
            keyNum+=2;
        }
        return block;
    }

    private int[] reverseMixing(int[] block, ArrayList<int[]> key) {
        int[] res = new int[block.length];

        int[] D0 = new int[4];
        int[] D1 = new int[4];
        int[] D2 = new int[4];
        int[] D3 = new int[4];

        int[] K0 = key.get(36);
        int[] K1 = key.get(37);
        int[] K2 = key.get(38);
        int[] K3 = key.get(39);

        for (int i = 0; i < block.length; i++) {
            if (0 <= i & i < 4) {
                D0[i] = block[i];
            }
            if (4 <= i & i < 8) {
                D1[i % 4] = block[i];
            }
            if (8 <= i & i < 12) {
                D2[i % 4] = block[i];
            }
            if (12 <= i & i < 16) {
                D3[i % 4] = block[i];
            }
        }

        for (int l = 0; l < 8; l++) {
            for (int j = 0; j < 2; j++) {

                //1 ФАЗА
                D1 = Tools.xor(D1, Tools.S1(D0[3]));
                D2 = Tools.modSub(D2, Tools.S0(D0[2]));
                D3 = Tools.modSub(D3, Tools.S1(D0[1]));
                D3 = Tools.xor(D3, Tools.S0(D0[0]));

                D0 = Tools.CSL(D0, 24);


                //2 ФАЗА
                D2 = Tools.xor(D2, Tools.S1(D1[3]));
                D3 = Tools.modSub(D3, Tools.S0(D1[2]));
                D0 = Tools.modSub(D0, Tools.S1(D1[1]));
                D0 = Tools.xor(D0, Tools.S0(D1[0]));

                D1 = Tools.CSL(D1, 24);

                D2 = Tools.modSub(D2, D1);

                //3 ФАЗА
                D3 = Tools.xor(D3, Tools.S1(D2[3]));
                D0 = Tools.modSub(D0, Tools.S0(D2[2]));
                D1 = Tools.modSub(D1, Tools.S1(D2[1]));
                D1 = Tools.xor(D1, Tools.S0(D2[0]));

                D2 = Tools.CSL(D2, 24);

                D3 = Tools.modSub(D3, D0);

                //4 ФАЗА
                D0 = Tools.xor(D0, Tools.S1(D3[3]));
                D1 = Tools.modSub(D1, Tools.S0(D3[2]));
                D2 = Tools.modSub(D2, Tools.S1(D3[1]));
                D2 = Tools.xor(D2, Tools.S0(D3[0]));

                D3 = Tools.CSL(D3, 24);
            }
        }

        D0 = Tools.modSub(D0, K0);
        D1 = Tools.modSub(D1, K1);
        D2 = Tools.modSub(D2, K2);
        D3 = Tools.modSub(D3, K3);

        for (int i = 0; i < res.length; i++) {
            if (0 <= i & i < 4) {
                res[i] = D0[i];
            }
            if (4 <= i & i < 8) {
                res[i] = D1[i % 4];
            }
            if (8 <= i & i < 12) {
                res[i] = D2[i % 4];
            }
            if (12 <= i & i < 16) {
                res[i] = D3[i % 4];
            }
        }

        return res;
    }

    private int[] directKeyMixing(int[] D0, int[] D1, int[] D2, int[] D3, int[] key1, int[] key2) {
        int[] block = new int[16];

        int[] Y1 = Tools.E(D0, key1).get(0);
        int[] Y2 = Tools.E(D0, key1).get(1);
        int[] Y3 = Tools.E(D0, key1).get(2);

        D0 = Tools.CSL(D0, 13);
        D1 = Tools.modSum(D1, Y1);
        D2 = Tools.modSum(D2, Y2);
        D3 = Tools.xor(D3, Y3);

        Y1 = Tools.E(D1, key2).get(0);
        Y2 = Tools.E(D1, key2).get(1);
        Y3 = Tools.E(D1, key2).get(2);

        D1 = Tools.CSL(D1, 13);
        D2 = Tools.modSum(D2, Y1);
        D3 = Tools.modSum(D3, Y2);
        D0 = Tools.xor(D0, Y3);

        for (int i = 0; i < block.length; i++) {
            if (0 <= i & i < 4) {
                block[i] = D2[i];
            }
            if (4 <= i & i < 8) {
                block[i] = D3[i % 4];
            }
            if (8 <= i & i < 12) {
                block[i] = D0[i % 4];
            }
            if (12 <= i & i < 16) {
                block[i] = D1[i % 4];
            }
        }

        return block;
    }

    private int[] reverseKeyMixing(int[] D0, int[] D1, int[] D2, int[] D3, int[] key1, int[] key2) {
        int[] block = new int[16];

        int[] Y1 = Tools.E(D0, key1).get(0);
        int[] Y2 = Tools.E(D0, key1).get(1);
        int[] Y3 = Tools.E(D0, key1).get(2);

        D0 = Tools.CSL(D0, 13);
        D3 = Tools.modSum(D3, Y1);
        D2 = Tools.modSum(D2, Y2);
        D1 = Tools.xor(D1, Y3);

        Y1 = Tools.E(D1, key2).get(0);
        Y2 = Tools.E(D1, key2).get(1);
        Y3 = Tools.E(D1, key2).get(2);

        D1 = Tools.CSL(D1, 13);
        D0 = Tools.modSum(D0, Y1);
        D3 = Tools.modSum(D3, Y2);
        D2 = Tools.xor(D2, Y3);

        for (int i = 0; i < block.length; i++) {
            if (0 <= i & i < 4) {
                block[i] = D2[i];
            }
            if (4 <= i & i < 8) {
                block[i] = D3[i % 4];
            }
            if (8 <= i & i < 12) {
                block[i] = D0[i % 4];
            }
            if (12 <= i & i < 16) {
                block[i] = D1[i % 4];
            }
        }

        return block;
    }


    public double correlation() {
        int[] open = openText;
        int[] enc = encryptedText;
        int count = 0;
        for (int i = 0; i < open.length; i++) {
            int encByte = enc[i] & 0b11111111;
            int openByte = open[i] & 0b11111111;
            for (int j = 0; j < 8; j++) {
                int tmpIn = (openByte >>> j) & 1;
                int tmpOut = (encByte >>> j) & 1;

                count += (2*tmpIn - 1)*(2*tmpOut - 1);
            }
        }
        double N = open.length*8;

        return (double)count / N;
    }

    public double distribution() {
        int[] enc = encryptedText;
        int count = 0;
        for(int i = 0; i < enc.length; i++) {
            int encByte = enc[i] & 0b11111111;
            for(int j = 0; j < 8; j++) {
                int tmp = (encByte >>> j) & 1;

                count += tmp;
            }
        }
        double N = enc.length*8;
        return (double)count / N;
    }
}
