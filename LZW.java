/**
 *  Author  :   Elbert Cheng
 *  Date    :   12/21/2019
 *  Version :   1.0.0
 *  Purpose :   To statically encode and decode files with a Lempel-Ziv-Welch compression algorithm
 *      which, when encoding, outputs the values with 9-bits then increments the bit length as it
 *      exceeds the limit. (9-bits for values 256-511, 10-bits for values 512-2047, etc.)
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class LZW {
    // The maximum value of an ASCII character
    private static final int MAX_CHAR = 256;
    // The 8 bits size of a byte
    private static final int BYTE_SIZE = 8;
    // The end of file character
    private static final char EOF = (char) -1;

    /******************
     * Public Methods *
     ******************/

    /**
     * Encodes the input file with the Lempel-Ziv-Welch compression algorithm then
     * outputs it into another file. Starts output 9-bit numbers then when the limit
     * is exceeded, it goes into 10, then 11, etc.
     *
     * @param in    The input file stream
     * @param out   The output BinaryOutputStream
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static void encode(FileInputStream in, BinaryOutputStream out) throws IOException {
        Map<String, Integer> map = initMap(true);

        int bitsNum = 9;
        int maxSize = (int) Math.pow(2, bitsNum);

        StringBuilder p = new StringBuilder();
        int index = MAX_CHAR;
        char c;

        while ((c = (char) in.read()) != EOF) {
            // Add char to string
            String pc = p.toString() + c;

            if (map.containsKey(pc)) {  // If map has key, add to the previous
                p.append(c);
            } else {    // If not, write previous to file, put new combination to map, reset previous
                out.writeBits(map.get(p.toString()), bitsNum);
                map.put(pc, index++);
                // Check whether or not to increment bits
                if (index == maxSize) maxSize = (int) Math.pow(2, ++bitsNum);
                // Set current to previous
                p = new StringBuilder("" + c);
            }
        }

        out.writeBits(map.get(p.toString()), bitsNum);
        // Flush out the last buffered number
        out.flush();
    }

    /**
     * Decodes a file compressed with this Lempel-Ziv-Welch algorithm then outputs the
     * result into another file.
     *
     * @param in    The input BinaryInputStream
     * @param out   The output file stream
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static void decode(BinaryInputStream in, FileOutputStream out) throws IOException {
        Map<Integer, String> map = initMap(false);

        int bitsNum = 9;
        int maxSize = (int) Math.pow(2, bitsNum);

        // Get first input code
        int old = in.readBits(bitsNum);

        // Get and print character translation
        char c = map.get(old).charAt(0);
        out.write(c);

        String s;
        int n;
        int index = MAX_CHAR;
        // Go through the rest of the file
        while ((n = in.readBits(bitsNum)) != -1) {
            if (!map.containsKey(n)) {  // If the table doesn't contain the code
                // Get the previous results
                s = map.get(old);
                // Combine with the current
                s += c;
                // This will output then be added to the map
            } else {
                // The current string is the result of the new code
                s = map.get(n);
            }

            // Output the result string, char-by-char
            for (int i = 0; i < s.length(); i++) {
                out.write(s.charAt(i));
            }

            // Move the pointer up to the next character
            c = s.charAt(0);
            // Add the new combination to the map
            map.put(index++, map.get(old) + c);

            // Declare the new code as old
            old = n;

            // Check whether or not to increment bits
            if (index == maxSize - 1) maxSize = (int) Math.pow(2, ++bitsNum);
        }
    }

    /**************************
     * Private Method Helpers *
     **************************/

    /**
     * Initializes the map for the encode or the decode based on the "encode" boolean.
     *
     * @param encode    Whether or not the map initialization is for the encode method or decode method
     * @return          A Map<String, Integer> or a Map<Integer, String></Integer,>
     */
    private static Map initMap(boolean encode) {
        Map map = (encode) ? new HashMap<String, Integer>() : new HashMap<Integer, String>();

        for (int i = 0; i < MAX_CHAR; i++) {
            if (encode) map.put("" + (char) i, i);
            else map.put(i, "" + (char) i);
        }

        return map;
    }
}
