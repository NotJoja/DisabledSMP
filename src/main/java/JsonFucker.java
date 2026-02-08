void main() throws IOException {

//    File dic = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledPack/assets/dissmp/items/disicons");
//    File[] pedoFiles = dic.listFiles();
//    for (File file : pedoFiles) {
//        File newFile = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledPack/assets/dissmp/items/gray_disicons/gray_" + file.getName());
//        replaceLine(file, newFile, 5, "\"dissmp:item/gray_disicons/gray_" + file.getName().split("\\.")[0] + "\"");
//    }

    File dic = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledSMP/assets/dissmp/textures/item/gray_cures");
    File[] pedoFiles = dic.listFiles();
    System.out.println(Arrays.toString(pedoFiles));
    for (File file : pedoFiles) {
        File copyOldFile = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledSMP/assets/dissmp/items/disicons/adhd.json");
        File newFile = new File("/home/nic/Programming/Minecraft-Plugins/DisabledSMP/src/main/resources/DisabledSMP/assets/dissmp/items/gray_cures/" + file.getName().split("\\.")[0] + ".json");
        newFile.createNewFile();
        replaceLine(copyOldFile, newFile, 5, "\"dissmp:item/gray_cures/" + file.getName().split("\\.")[0] + "\"");
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
