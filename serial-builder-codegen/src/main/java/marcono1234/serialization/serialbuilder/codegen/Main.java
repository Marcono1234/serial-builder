package marcono1234.serialization.serialbuilder.codegen;

import java.util.HexFormat;

/**
 * Main class for usage from command line. Prefer using {@link SimpleSerialBuilderCodeGen} directly when generating
 * the code programmatically.
 */
public class Main {
    private Main() {
    }

    public static void main(String... args) throws CodeGenException {
        if (args.length != 2 || !args[0].equals("simple-api")) {
            System.out.println("Usage: simple-api <serial-data-hex>");
            System.out.println();
            System.out.println("  Example: java -jar serial-builder-codegen.jar simple-api aced0005737200116a6176612e6c616e672e426f6f6c65616ecd207280d59cfaee0200015a000576616c7565787001");
            System.exit(1);
        }

        byte[] serialData = HexFormat.of().parseHex(args[1]);
        String code = SimpleSerialBuilderCodeGen.generateCode(serialData);
        System.out.println(code);
    }
}
