void main() throws IOException {

//    File dic = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledPack/assets/dissmp/items/disicons");
//    File[] pedoFiles = dic.listFiles();
//    for (File file : pedoFiles) {
//        File newFile = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledPack/assets/dissmp/items/gray_disicons/gray_" + file.getName());
//        replaceLine(file, newFile, 5, "\"dissmp:item/gray_disicons/gray_" + file.getName().split("\\.")[0] + "\"");
//    }

    File dic = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledPack/assets/dissmp/models/item/disicons");
    File[] pedoFiles = dic.listFiles();
    for (File file : pedoFiles) {
        File newFile = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledPack/assets/dissmp/models/item/gray_disicons/gray_" + file.getName());
        replaceLine(file, newFile, 4, "\"layer0\": \"dissmp:item/gray_disicons/gray_" + file.getName().split("\\.")[0] + "\"");
    }
}

void replaceLine(File oldFile, File newFile, int lineNumber, String newLine) throws IOException {
    List<String> lines = Files.readAllLines(oldFile.toPath());

    if (lineNumber < 1 || lineNumber > lines.size()) {
        throw new IllegalArgumentException("Invalid line number");
    }

    lines.set(lineNumber - 1, newLine);
    Files.write(newFile.toPath(), lines);
}
