import components.map.Map;
import components.map.Map1L;
import components.queue.Queue;
import components.queue.Queue2;
import components.sequence.Sequence;
import components.sequence.Sequence1L;
import components.set.Set;
import components.set.Set1L;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;

/**
 * Generate a glossary facility.
 *
 * @author Xiaoke Wang
 */
public final class GlossaryFacility {

    /**
     * Default constructor--private to prevent instantiation.
     */
    private GlossaryFacility() {
        // no code needed here
    }

    /**
     * Read terms name from the given file, store them in Sequence in
     * alphabetical order and also store them in Set.
     *
     * @param filename
     *            the given file name
     * @param words
     *            the Sequence that stores term name
     * @param index
     *            the Set that stores term name
     */
    public static void readWords(String filename, Sequence<String> words,
            Set<String> index) {
        SimpleReader in = new SimpleReader1L(filename);

        // Read the file line by line:
        while (!in.atEOS()) {
            // Read the term name:
            String word = in.nextLine();

            // Skip the term definition part:
            String tmp = in.nextLine();
            while (!tmp.equals("")) {
                tmp = in.nextLine();
            }

            // Add the term name in the sequence and set:
            if (words.length() == 0) {
                words.add(0, word);
            } else {
                // Insert the term name in alphabetical order:
                int key = words.length();
                for (int i = words.length() - 1; i >= 0; i--) {
                    if (word.compareToIgnoreCase(words.entry(i)) < 0) {
                        key = i;
                    }
                }

                words.add(key, word);
            }
            index.add(word);
        }
        in.close();
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";

        for (int i = 0; i < str.length(); i++) {
            // Add the character one by one:
            char tmp = str.charAt(i);
            // If that character is not in Set, add it:
            if (!charSet.contains(tmp)) {
                charSet.add(tmp);
            }
        }

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        String str = "";

        int i = position;
        while (i < text.length()) {

            Character c = text.charAt(i);

            // If that character is not a separator:
            if (!separators.contains(c)) {
                str = str + c;
            } else { // c is a separator:
                if (str.length() == 0) {
                    str = str + c;
                }
                i = text.length() - 1;
            }
            i++;
        }

        // Return extracted string from that position:
        return str;
    }

    /**
     * Modify the definition, add the link to the name of the term.
     *
     * @param str
     *            the given definition.
     * @param separatorSet
     *            the Set that contains separators.
     * @param index
     *            the Set that contains terms name.
     * @return the modified definition.
     *
     */
    public static String modifyDefinitions(String str,
            Set<Character> separatorSet, Set<String> index) {

        String s = "";

        int position = 0;
        while (position < str.length()) {
            String token = nextWordOrSeparator(str, position, separatorSet);

            // If that token string is a term name, add a link:
            if (index.contains(token)) {
                s = s + "<a href=\"" + token + ".html\">" + token + "</a>";
            } else { // Token string is not term name:
                s = s + token;
            }
            position += token.length();
        }

        return s;
    }

    /**
     * Read the definition for each term and store them in a Map.
     *
     * @param filename
     *            the given file name.
     * @param words
     *            the Sequence contains term name in order.
     * @param definitions
     *            the Map contains term name and definition.
     * @param index
     *            the Set contains term name.
     */
    public static void getDefinitions(String filename, Sequence<String> words,
            Map<String, String> definitions, Set<String> index) {
        SimpleReader in = new SimpleReader1L(filename);

        // Generate the Set contains separators:
        Set<Character> separatorSet = new Set1L<Character>();
        generateElements(" ,.?!()", separatorSet);

        // Read the definition, modify it and store it in a Map:
        while (!in.atEOS()) {
            // Read the definition line by line:
            Queue<String> combineDef = new Queue2<String>();
            String word = in.nextLine();
            String tmp = in.nextLine();
            while (!tmp.equals("")) {
                combineDef.enqueue(tmp);
                tmp = in.nextLine();
            }
            // Combine those definition fragments in one string:
            String d = "";
            while (combineDef.length() > 0) {
                d = d + combineDef.dequeue();
            }

            // Modify and store:
            d = modifyDefinitions(d, separatorSet, index);
            definitions.add(word, d);
        }

        in.close();
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * @param out
     *            the output stream
     * @updates out.content
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    public static void outputHeader(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        out.println("<html>");
        out.println("<head>");

        out.println("<title>" + "Glossary" + "</title>");

        out.println("</head>");
        out.println("<body>");

        out.println(" <h2>" + "Glossary" + "</h2>");

        out.println(" <hr>");

        out.println(" <h3>" + "Index" + "</h3>");

    }

    /**
     * Outputs the html file footer.
     *
     * @param out
     *            the output stream.
     */
    public static void outputFooter(SimpleWriter out) {

        out.println("</body> </html>");

    }

    /**
     * Generate term's definition page.
     *
     * @param foldername
     *            the output folder name.
     * @param name
     *            the term name for that page.
     * @param definitions
     *            the Map contains term name and definition.
     */
    public static void generateDefinitons(String foldername, String name,
            Map<String, String> definitions) {

        SimpleWriter out = new SimpleWriter1L(
                foldername + "/" + name + ".html");

        out.println("<html>");
        out.println("<head>");

        out.println("<title>" + name + "</title>");

        out.println("</head>");
        out.println("<body>");

        out.println(" <h2 style=\"color:red; font-family:boldface\">" + "<i>"
                + name + "</i>" + "</h2>");

        out.println(
                " <blockquote>" + definitions.value(name) + "</blockquote>");

        out.println(" <hr>");

        out.println(" <p>" + "Return to " + "<a href=\"" + "index.html" + "\">"
                + "index" + "</a>" + "</p>");

        out.println("</body> </html>");

        out.close();
    }

    /**
     * Generate the term list in the index page.
     *
     * @param out
     *            the output stream.
     * @param filename
     *            the output file name.
     * @param foldername
     *            the output folder name.
     */
    public static void generateLists(SimpleWriter out, String filename,
            String foldername) {

        SimpleReader readfile = new SimpleReader1L(filename);

        Sequence<String> words = new Sequence1L<String>();
        Map<String, String> definitions = new Map1L<String, String>();
        Set<String> index = new Set1L<String>();

        // Read term names and store them:
        readWords(filename, words, index);

        // Read term definitions and store it:
        getDefinitions(filename, words, definitions, index);

        // Output the list:
        out.println(" <ul>");

        for (int i = 0; i < words.length(); i++) {
            generateDefinitons(foldername, words.entry(i), definitions);
            out.println("  <li>" + "<a href=\"" + words.entry(i) + ".html"
                    + "\">" + words.entry(i) + "</a>" + "</li>");
        }

        out.println(" </ul>");

        readfile.close();

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        // Ask user for the file name:
        out.print("Please input the glossary file name: ");
        String filename = in.nextLine();

        // Ask user for the folder name:
        out.print("Please input the output folder name: ");
        String foldername = in.nextLine();
        SimpleWriter writefile = new SimpleWriter1L(foldername + "/index.html");

        // Generate index page:
        outputHeader(writefile);

        generateLists(writefile, filename, foldername);

        outputFooter(writefile);

        writefile.close();

        in.close();
        out.close();
    }

}
