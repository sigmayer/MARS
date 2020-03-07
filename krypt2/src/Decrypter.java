import java.io.*;
import java.util.ArrayList;

public class Decrypter {

    private int[] encryptedText;
    public int[] decryptedText;
    private int fileSize;
    private ArrayList<int[]> key;

    public Decrypter(String fileName, int[] k, int fileSize) {
        encryptedText = readBytes(fileName);
        key = Tools.makeKey(k);
        int num = 0;
        for (int i = encryptedText.length-1; i >= encryptedText.length-4; i--) {
            num ^= encryptedText[i];
            num = num << 8;
        }
        num = num >> 8;
        this.fileSize = num;
    }

    public int[] decryption() {

        int[] block = new int[16];
        ArrayList<int[]> blocks= new ArrayList<>();

        for (int i = 0; i < encryptedText.length; i++) {
            if ((i % 16 == 0) & (i != 0)) {
                blocks.add(block);
                block = new int[16];
            }
            block[i % 16] = encryptedText[i];
        }
        blocks.add(block);

        for (int i = 0; i < blocks.size(); i++) {
            int[] encryptedBlock = reverseMixing(blocks.get(i), key);
            blocks.set(i, encryptedBlock);
        }
        for (int i = 0; i < blocks.size(); i++) {
            int[] encryptedBlock = decryptCore(blocks.get(i), key);
            blocks.set(i, encryptedBlock);
        }
        for (int i = 0; i < blocks.size(); i++) {
            int[] encryptedBlock = directMixing(blocks.get(i), key);
            blocks.set(i, encryptedBlock);
        }

        int count = 0;
        decryptedText = new int[fileSize];
        for (int i = 0; i < blocks.size(); i++) {
            for (int j = 0; j < blocks.get(i).length; j++) {
                if (count < fileSize) {
                    decryptedText[count] = blocks.get(i)[j];
                }
                count++;
            }
        }
        writeBytes("C:/Users/123/IdeaProjects/krypt2/src/dec.bmp", fileSize);
        return decryptedText;
    }

    public int[] readBytes(String fileName) {
        int[] res;
        try {
            File file = new File(fileName);
            res = new int[(int)file.length()];
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

    public void writeBytes(String fileName, int fileSize) {
        try {
            File file = new File(fileName);
            FileOutputStream writer = new FileOutputStream(file);
            BufferedOutputStream stream = new BufferedOutputStream(writer);
            byte[] res = new byte[fileSize];
            for (int i = 0; i < fileSize; i++) {
                res[i] = (byte)(decryptedText[i] & 0xff);
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

        for (int l = 0; l < 8; l++) {
            for (int j = 0; j < 2; j++) {

                //1 ФАЗА
                D3 = Tools.CSP(D3, 8);

                D2 = Tools.xor(D2, Tools.S1(D3[0]));
                D1 = Tools.modSub(D1, Tools.S0(D3[1]));
                D0 = Tools.modSub(D0, Tools.S1(D3[2]));
                D0 = Tools.xor(D0, Tools.S0(D3[3]));

                //2 ФАЗА
                D2 = Tools.CSP(D2, 8);

                D3 = Tools.modSub(D3, Tools.S1(D2[2]));
                D3 = Tools.xor(D3, Tools.S0(D2[3]));
                D1 = Tools.xor(D1, Tools.S1(D2[0]));
                D0 = Tools.modSub(D0, Tools.S0(D2[1]));

                //3 ФАЗА
                D1 = Tools.modSub(D1, D2);

                D1 = Tools.CSP(D1, 8);

                D3 = Tools.modSub(D3, Tools.S0(D1[1]));
                D2 = Tools.modSub(D2, Tools.S1(D1[2]));
                D2 = Tools.xor(D2, Tools.S0(D1[3]));
                D0 = Tools.xor(D0, Tools.S1(D1[0]));

                //4 ФАЗА
                D0 = Tools.modSub(D0, D3);

                D0 = Tools.CSP(D0, 8);

                D3 = Tools.xor(D3, Tools.S1(D0[0]));
                D2 = Tools.modSub(D2, Tools.S0(D0[1]));
                D1 = Tools.modSub(D1, Tools.S1(D0[2]));
                D1 = Tools.xor(D1, Tools.S0(D0[3]));

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

    private int[] decryptCore(int[] block, ArrayList<int[]> key) {

        int[] D0 = new int[4];
        int[] D1 = new int[4];
        int[] D2 = new int[4];
        int[] D3 = new int[4];

        int keyNum = 35;
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
            block = reverseKeyMixing(D0, D1, D2, D3, key.get(keyNum), key.get(keyNum-1));
            keyNum-=2;
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
            block = directKeyMixing(D0, D1, D2, D3, key.get(keyNum), key.get(keyNum-1));
            keyNum-=2;
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

        D0 = Tools.modSum(D0, K0);
        D1 = Tools.modSum(D1, K1);
        D2 = Tools.modSum(D2, K2);
        D3 = Tools.modSum(D3, K3);

        for (int l = 0; l < 8; l++) {
            for (int j = 0; j < 2; j++) {

                //1 ФАЗА
                D3 = Tools.CSL(D3, 8);

                D2 = Tools.xor(D2, Tools.S0(D3[0]));
                D2 = Tools.modSum(D2, Tools.S1(D3[1]));
                D1 = Tools.modSum(D1, Tools.S0(D3[2]));
                D0 = Tools.xor(D0, Tools.S1(D3[3]));

                //2 ФАЗА
                D3 = Tools.modSum(D3, D0);

                D2 = Tools.CSL(D2, 8);

                D1 = Tools.xor(D1, Tools.S0(D2[0]));
                D1 = Tools.modSum(D1, Tools.S1(D2[1]));
                D0 = Tools.modSum(D0, Tools.S0(D2[2]));
                D3 = Tools.xor(D3, Tools.S1(D2[3]));

                //3 ФАЗА
                D2 = Tools.modSum(D2, D1);

                D1 = Tools.CSL(D1, 8);

                D0 = Tools.xor(D0, Tools.S0(D1[0]));
                D0 = Tools.modSum(D0, Tools.S1(D1[1]));
                D3 = Tools.modSum(D3, Tools.S0(D1[2]));
                D2 = Tools.xor(D2, Tools.S1(D1[3]));

                //4 ФАЗА
                D0 = Tools.CSL(D0, 8);

                D3 = Tools.xor(D3, Tools.S0(D0[0]));
                D3 = Tools.modSum(D3, Tools.S1(D0[1]));
                D2 = Tools.modSum(D2, Tools.S0(D0[2]));
                D1 = Tools.xor(D1, Tools.S1(D0[3]));
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

    private int[] directKeyMixing(int[] D0, int[] D1, int[] D2, int[] D3, int[] key1, int[] key2) {
        int[] block = new int[16];

        D3 = Tools.CSL(D3, 19);

        int[] Y1 = Tools.E(D3, key1).get(0);
        int[] Y2 = Tools.E(D3, key1).get(1);
        int[] Y3 = Tools.E(D3, key1).get(2);

        D0 = Tools.modSub(D0, Y1);
        D1 = Tools.modSub(D1, Y2);
        D2 = Tools.xor(D2, Y3);

        D2 = Tools.CSL(D2, 19);

        Y1 = Tools.E(D2, key2).get(0);
        Y2 = Tools.E(D2, key2).get(1);
        Y3 = Tools.E(D2, key2).get(2);

        D3 = Tools.modSub(D3, Y1);
        D0 = Tools.modSub(D0, Y2);
        D1 = Tools.xor(D1, Y3);

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

        D3 = Tools.CSL(D3, 19);

        int[] Y1 = Tools.E(D3, key1).get(0);
        int[] Y2 = Tools.E(D3, key1).get(1);
        int[] Y3 = Tools.E(D3, key1).get(2);

        D2 = Tools.modSub(D2, Y1);
        D1 = Tools.modSub(D1, Y2);
        D0 = Tools.xor(D0, Y3);

        D2 = Tools.CSL(D2, 19);

        Y1 = Tools.E(D2, key2).get(0);
        Y2 = Tools.E(D2, key2).get(1);
        Y3 = Tools.E(D2, key2).get(2);

        D1 = Tools.modSub(D1, Y1);
        D0 = Tools.modSub(D0, Y2);
        D3 = Tools.xor(D3, Y3);

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
}
