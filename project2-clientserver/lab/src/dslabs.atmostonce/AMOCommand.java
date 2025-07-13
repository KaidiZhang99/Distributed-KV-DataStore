package dslabs.atmostonce;

import dslabs.framework.Command;
import lombok.Data;

@Data
public final class AMOCommand implements Command {
    //TODO: implement your wrapper for command
    //Hints: think carefully about what information is required for server to check duplication

    final private int sequenceNum;
    final Command command;
    final private String client;


}
