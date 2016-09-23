package org.ccls.nlp.cbt.utils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Word2VecSimilarity {

	private long words;	
	private long size;
	private List<String> vocab;
	private List<Float[]> values;
	private Set<Entry<String, Float>> results;

	public static final Integer BEST_MATCHES_TO_RETURN = 40;

	public Word2VecSimilarity() {
		vocab = null;
		values = null;
		results = null;
	}

	// assume byteArray to be length 4
	// WE NEED LITTLE_ENDIAN!!!!!!!!!!!!!!!!
	// THE DEFAULT IS BIG_ENDIAN, SO WE MUST TELL ByteBuffer!!!!!!!!!!!!!!!!
	private float byteArrayToFloat(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.getFloat();
	}

	public void setupVectors() {
		String filename = "word2vec/vectors.bin";
		Path path = FileSystems.getDefault().getPath(".", filename);
		try {
			byte[] content = Files.readAllBytes(path);
			// the first two values should be long longs (64 bit values)
			// they represent words and size respectively
			words = 0L;
			size = 0L;
			int pointer = 0;
			while (true) {
				if (content[pointer] == 32) break;
				words = words * 10 + (content[pointer] - 48);
				pointer++;
			}
			pointer++;
			while (true) {
				if (content[pointer] == 10) break;
				size = size * 10 + (content[pointer] - 48);
				pointer++;
			}
			pointer++;

			//********************************************************
			// This code forms the vocab list and the associated vectors

			StringBuilder thisWord = new StringBuilder("");
			vocab = new ArrayList<String>();
			values = new ArrayList<Float[]>();
			for (int index = 0; index < words; index++) {
				Float[] theseValues = new Float[(int) size];
				thisWord.setLength(0);
				while (true) {
					if (content[pointer] == 32) break;
					thisWord.append((char) content[pointer]);
					pointer++;
				}
				pointer++;
				vocab.add(thisWord.toString());
				for (int index2 = 0; index2 < size; index2++) {
					byte[] theseBytes = Arrays.copyOfRange(content, pointer, pointer + 4);
					float thisValue = byteArrayToFloat(theseBytes);
					theseValues[index2] = thisValue;
					pointer += 4;
				}
				pointer++;
				values.add(theseValues);
			}	

			//*************************************************************
			// Now we normalize the value vectors for each word

			for (Float[] valueVector : values) {
				float len = 0.0f;
				for (Float singleValue : valueVector) {
					len += Math.pow(singleValue, 2.0);
				}
				len = (float) Math.sqrt(len);
				for (int index = 0; index < valueVector.length; index++) {
					valueVector[index] /= len;
				}
			}
		} catch (IOException ioe) {
			words = 0L;
			size = 0L;
			System.err.println(ioe.getMessage());
		}
	}

	public boolean getSimilarVectors(String target) {
		// 1st, find word, or determine if it is OOV
		int wordPosition = vocab.indexOf(target);
		if (wordPosition == -1) {
			return false;
		}

		// 2nd we get the correct value vector for the target word
		Float[] targetVector = values.get(wordPosition);

		// 3rd calculate cosine similarities involving all the normalized vectors

		Map<String, Float> similaritiesMap = new HashMap<String, Float>();

		for (int index = 0; index < vocab.size(); index++) {
			if (index == wordPosition) continue;
			float dist = 0.0f;
			Float[] vocabVector = values.get(index);
			for (int index2 = 0; index2 < size; index2++) {
				dist += vocabVector[index2] * targetVector[index2];
			}
			similaritiesMap.put(vocab.get(index), dist);
		}

		// 4th rank them according to the constant BEST_MATCHES_TO_RETURN
		similaritiesMap = MapUtils.sortByValue(similaritiesMap);
		results = similaritiesMap.entrySet();
		/*int resultSetSize = results.size();
		int display = Math.min(resultSetSize, BEST_MATCHES_TO_RETURN);
		int soFar = 0;
		System.out.println("Word\tValue");
		System.out.println("***********");
		for (Entry<String, Float> result : results) {
			System.out.println(result.getKey() + "\t" + result.getValue());
			soFar++;
			if (soFar >= display) break;
		}*/
		
		return true;
	}

	public List<String> getBest(int limit) {
		int resultSetSize = results.size();
		int display = Math.min(resultSetSize, BEST_MATCHES_TO_RETURN);
		display = Math.min(display, limit);
		int soFar = 0;
		List<String> toReturn = new ArrayList<String>();
		for (Entry<String, Float> result : results) {
			toReturn.add(result.getKey());
			soFar++;
			if (soFar >= display) break;
		}
		return toReturn;
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java Word2VecSimilarity <string>");
		}
		Word2VecSimilarity da8 = new Word2VecSimilarity();
		da8.setupVectors();
		da8.getSimilarVectors(args[0].toLowerCase());	    
	}

}
