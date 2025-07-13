package dslabs.atmostonce;

import lombok.Data;
import dslabs.framework.Command;


@Data
public final class AMOCommand implements Command {
    //Hints: think carefully about what information is required for server to check duplication
    final private String client;
    final private int sequenceNum;
    final Command command;
}
