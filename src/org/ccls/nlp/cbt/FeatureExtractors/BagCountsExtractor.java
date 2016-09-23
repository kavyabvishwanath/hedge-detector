package org.ccls.nlp.cbt.FeatureExtractors;
/** 
 * Copyright (c) 2007-2010, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.CleartkExtractorException;
import org.cleartk.classifier.feature.extractor.filter.AnnotationFilter;
import org.cleartk.classifier.feature.extractor.simple.SimpleFeatureExtractor;
import org.uimafit.util.JCasUtil;

/**
 * <br>
 * Copyright (c) 2007-2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * A class for extracting bag-of-words style features. It allows any type of Annotation to serve as
 * the "words", and allows any type of feature extractor to be applied to each of these "words".
 * 
 * @author Steven Bethard, Philipp Wetzler
 */
public class BagCountsExtractor implements SimpleFeatureExtractor {

	private Class<? extends Annotation> annotationClass;

	private SimpleFeatureExtractor subExtractor;

	private AnnotationFilter filter;

	private String featureName;

	public BagCountsExtractor(
			Class<? extends Annotation> annotationClass,
			SimpleFeatureExtractor subExtractor) {
		this.annotationClass = annotationClass;
		this.subExtractor = subExtractor;
		this.featureName = String.format("BagCounts(%s)", annotationClass.getSimpleName());
	}

	public List<Feature> extract(JCas jCas, Annotation focusAnnotation)
			throws CleartkExtractorException {
		List<Feature> features = new ArrayList<Feature>();

		Hashtable<String,Integer> featureHash = new Hashtable<String,Integer>();

		for (Annotation ann : JCasUtil.selectCovered(jCas, this.annotationClass, focusAnnotation)) {
			List<Feature> subFeatures = subExtractor.extract(jCas, ann);
			for (Feature f : subFeatures) {
				if (!featureHash.containsKey(f.getName()+":"+f.getValue())) {
					featureHash.put(f.getName()+":"+f.getValue(), 1);
				}
				else {
					featureHash.put(f.getName()+":"+f.getValue(), featureHash.get(f.getName()+":"+f.getValue()) + 1);
				}
			}
		}

		Enumeration<String> en = featureHash.keys();

		while (en.hasMoreElements()) {
			String thisFeatName = en.nextElement();
			features.add(new Feature(this.featureName + ":" + thisFeatName, featureHash.get(thisFeatName)));
		}

		return features;
	}

}