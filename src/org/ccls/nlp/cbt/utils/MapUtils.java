package org.ccls.nlp.cbt.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class MapUtils {

	/**
	 * Note: THIS IS A JAVA7 METHOD.  Don't use this class if you can't use it.
	 * @param map - A map to be sorted
	 * @return - A map sorted by value
	 */
	public static <K, V extends Comparable<? super V>> Map<K, V> 
	sortByValue( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list =
				new LinkedList<>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
				{
			@Override
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o2.getValue()).compareTo( o1.getValue() );
			}
				} );

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	// TEST!!!!!!
	public static void main(String[] args) {
		Map<String, Float> myMap = new HashMap<String, Float>();
		myMap.put("Yellow", 5.3f);
		myMap.put("Gold", 3.7f);
		myMap.put("Canary", 7.1f);
		myMap.put("Maize", 4.8f);
		myMap.put("Jaune", 2.9f);
		System.out.println(MapUtils.sortByValue(myMap));
	}

}
