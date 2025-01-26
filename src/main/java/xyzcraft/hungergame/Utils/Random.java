package xyzcraft.hungergame.Utils;

import java.util.List;

public class Random {
    public static String getRandomMessage(List<String> messages) {

        java.util.Random random = new java.util.Random();


        int index = random.nextInt(messages.size());


        return messages.get(index);
    }
}
