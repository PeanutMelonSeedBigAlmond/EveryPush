import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class RemoveSignCache {
    public static void main(String[] args) throws Exception {
        String target;
        if (args.length == 0) {
            target = "app/signTarget";
        } else {
            target = args[0];
        }

        File[] fList = (new File(target)).listFiles();
        for (File f : fList) {
            if (!f.getName().endsWith("-signed.apk")) {
                System.out.println("Deleting " + f.getName());
                f.delete();
            }
        }
    }
}