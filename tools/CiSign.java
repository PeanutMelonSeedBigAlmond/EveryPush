import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class CiSign {
    public static void main(String[] args) {
        String target;
        if (args.length==0){
            target="app/signTarget";
        }else {
            target=args[0];
        }
        File targetDir = new File(target);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        List<File> list = Stream.of((new File("app/build/outputs/apk")).listFiles())
                .flatMap((file) -> {
                    File f = new File(file, "release");
                    if (f.exists()) {
                        return Stream.of(f.listFiles((dir, name) -> name.endsWith(".apk")));
                    } else {
                        return Stream.of(new File[0]);
                    }
                }).collect(Collectors.toList());

        list.stream().forEach((file) -> {
            try {
                String fileName = file.getName().replace("unsigned", "signed");
                System.out.println("Copying "+fileName);
                Files.copy(file.toPath(), new File(targetDir, fileName).toPath());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }
}