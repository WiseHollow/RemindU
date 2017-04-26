package net.johnbrooks.remindu.util;

import java.util.Random;

/**
 * Created by John on 2/10/2017.
 */

public class Quotes
{
    private static Random random = new Random();
    private static String[] quotes = { "\"By failing to prepare, you are preparing to fail.\" - Benjamin Franklin",
                                        "\"A goal without a plan is just a wish.\" - Antoine de Saint-Exupéry",
                                        "\"Give me six hours to chop down a tree and I will spend the first four sharpening the axe.\" - Abraham Lincoln",
                                        "\"If you don’t know exactly where you’re going, how will you know when you get there?\" - Steve Maraboli",
                                        "\"Your mind is for having ideas, not holding them.\" + David Allen"
                                        };

    public static String GetRandom()
    {
        return quotes[random.nextInt(quotes.length - 1)];
    }
}
