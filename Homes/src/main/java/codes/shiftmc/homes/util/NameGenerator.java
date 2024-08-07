package codes.shiftmc.homes.util;

import java.util.Random;

/**
 * Just did it for fun. :|
 *
 * @author amit
 */
public class NameGenerator {
    private static final int diffBetweenAtoZ = 25;
    private static final int charValueOfa = 97;
    int length;
    char[] vowels = {
            'a', 'e', 'i', 'o', 'u'
    };
    private String lastGeneratedName = "";

    public NameGenerator(int lengthOfName) {
        if (lengthOfName < 5 || lengthOfName > 18) {
            lengthOfName = 7;
        }

        this.length = lengthOfName;
    }

    public String getName() {
        for (; ; ) {
            Random randomNumberGenerator = new Random();

            char[] nameInCharArray = new char[length];

            for (int i = 0; i < length; i++) {
                if (positionIsOdd(i)) {
                    nameInCharArray[i] = getVowel(randomNumberGenerator);
                } else {
                    nameInCharArray[i] = getConsonant(randomNumberGenerator);
                }
            }
            nameInCharArray[0] = Character
                    .toUpperCase(nameInCharArray[0]);

            String currentGeneratedName = new String(nameInCharArray);

            if (!currentGeneratedName.equals(lastGeneratedName)) {
                lastGeneratedName = currentGeneratedName;
                return currentGeneratedName;
            }

        }

    }

    private boolean positionIsOdd(int i) {
        return i % 2 == 0;
    }

    private char getConsonant(Random randomNumberGenerator) {
        for (; ; ) {
            char currentCharacter = (char) (randomNumberGenerator
                    .nextInt(diffBetweenAtoZ) + charValueOfa);
            if (currentCharacter == 'a' || currentCharacter == 'e'
                    || currentCharacter == 'i' || currentCharacter == 'o'
                    || currentCharacter == 'u')
                continue;
            else
                return currentCharacter;
        }

    }

    private char getVowel(Random randomNumberGenerator) {
        return vowels[randomNumberGenerator.nextInt(vowels.length)];
    }
}