package org.ccls.nlp.cbt.utils;


public class Syllables {

	// A method to count the number of syllables in a word
	// Pretty basic, just based off of the number of vowels
	// This could be improved
	public static int countSyllables(String word) {
		int      syl    = 0;
		boolean  vowel  = false;
		int      length = word.length();

		//check each word for vowels (don't count more than one vowel in a row)
		for(int i=0; i<length; i++) {
			if (isVowel(word.charAt(i)) && (vowel==false)) {
				vowel = true;
				syl++;
			} else if (isVowel(word.charAt(i)) && (vowel==true)) {
				vowel = true;
			} else {
				vowel = false;
			}
		}

		char tempChar = word.charAt(word.length()-1);
		//check for 'e' at the end, as long as not a word w/ one syllable
		if (((tempChar == 'e') || (tempChar == 'E')) && (syl != 1)) {
			syl--;
		}
		return syl;
	}

	//check if a char is a vowel (count y)
	private static boolean isVowel(char c) {
		if      ((c == 'a') || (c == 'A')) { return true;  }
		else if ((c == 'e') || (c == 'E')) { return true;  }
		else if ((c == 'i') || (c == 'I')) { return true;  }
		else if ((c == 'o') || (c == 'O')) { return true;  }
		else if ((c == 'u') || (c == 'U')) { return true;  }
		else if ((c == 'y') || (c == 'Y')) { return true;  }
		else                               { return false; }
	}
	
}
