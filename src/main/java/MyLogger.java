import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyLogger {
    private final File file;

    public MyLogger(String file){
        this.file = new File(file);
    }

    public void log(String message){
        System.out.println(message);

        try(FileWriter writer = new FileWriter(file, true)) {
            BufferedWriter bufferWriter = new BufferedWriter(writer);

            bufferWriter.write(message + "\n");
            bufferWriter.close();
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
