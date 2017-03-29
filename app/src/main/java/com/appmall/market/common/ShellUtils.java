package com.appmall.market.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

/**
 * ShellUtils
 */
public class ShellUtils {
    public static final String COMMAND_SH       = "sh";
    private static final String COMMAND_SU = "su";
    
    private static final String COMMAND_EXIT     = "exit\n";
    private static final String COMMAND_LINE_END = "\n";


    public static CommandResult execCommand(String command) {
        return execCommand(new String[] { command }, true);
    }

    public static CommandResult execCommand(List<String> commands) {
        return execCommand(commands == null ? null : commands.toArray(new String[] {}), true);
    }

    public static CommandResult execCommand(String[] commands) {
        return execCommand(commands, true);
    }

    public static CommandResult execCommand(String command, boolean isNeedResultMsg) {
        return execCommand(new String[] { command }, isNeedResultMsg);
    }

    public static CommandResult execCommand(List<String> commands, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : commands.toArray(new String[] {}), isNeedResultMsg);
    }

    public static CommandResult execCommand(String[] commands, boolean isNeedResultMsg) {
    	return doExecCommand(COMMAND_SH, commands, isNeedResultMsg);
    }
    
    public static CommandResult execSuperUserCommand(String command) {
    	return execSuperUserCommand(new String[] { command }, true);
	}
    
    public static CommandResult execSuperUserCommand(String[] commands, boolean isNeedResultMsg) {
		return doExecCommand(COMMAND_SU, commands, isNeedResultMsg);
	}
    
    private static CommandResult doExecCommand(String shell, String[] commands, boolean isNeedResultMsg) {
    	int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        InputStream in = null;
        InputStream error = null;
        OutputStream out = null;
        try {
        	process = Runtime.getRuntime().exec(shell);
            out = process.getOutputStream();
            DataOutputStream os = new DataOutputStream(out);
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
            	in = process.getInputStream();
            	error = process.getErrorStream();
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                BufferedReader successResult = new BufferedReader(new InputStreamReader(in));
                BufferedReader errorResult = new BufferedReader(new InputStreamReader(error));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                if (in != null)
                	in.close();
                if (out != null)
                	out.close();
                if (error != null)
                	error.close();
            } catch (IOException e) { }

            if (process != null)
                process.destroy();
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
            : errorMsg.toString());
    }

    /**
     * result of command,
     * <ul>
     * <li>{@link CommandResult#result} means result of command, 0 means normal, else means error, same to excute in
     * linux shell</li>
     * <li>{@link CommandResult#successMsg} means success message of command result</li>
     * <li>{@link CommandResult#errorMsg} means error message of command result</li>
     * </ul>
     */
    public static class CommandResult {

        /** result of command **/
        public int    result;
        /** success message of command result **/
        public String successMsg;
        /** error message of command result **/
        public String errorMsg;

        public CommandResult(int result){
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg){
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }

}
