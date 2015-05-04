/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Arman Bilge
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.armanbilge;

import dr.evolution.util.TaxonList;
import dr.evomodel.treelikelihood.TipStatesModel;
import dr.xml.AbstractXMLObjectParser;
import dr.xml.AttributeRule;
import dr.xml.ElementRule;
import dr.xml.XMLObject;
import dr.xml.XMLObjectParser;
import dr.xml.XMLParseException;
import dr.xml.XMLSyntaxRule;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Arman Bilge
 */
public class PartialsAlignmentModel extends TipStatesModel {

    private static final String PARTIALS_ALIGNMENT = "partialsAlignment";

    private final double[][][] partials;

    public PartialsAlignmentModel(final TaxonList taxa, final double[][][] partials) {
        super(PARTIALS_ALIGNMENT, taxa, null);
        this.partials = partials;
    }

    @Override
    protected void taxaChanged() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Type getModelType() {
        return Type.PARTIALS;
    }

    @Override
    public void getTipPartials(final int i, final double[] partials) {
        System.arraycopy(this.partials[i], 0, partials, 0, partials.length);
    }

    @Override
    public void getTipStates(int i, int[] ints) {
        throw new IllegalArgumentException("This model emits only tip partials");
    }

    public static final XMLObjectParser PARSER = new AbstractXMLObjectParser() {

        private static final String SEQUENCE = "sequence";
        private static final String PARTIAL = "partial";
        private static final String PROBABILITIES = "p";

        @Override
        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            final TaxonList taxa = (TaxonList) xo.getChild(TaxonList.class);
            final List<double[][]> alignment = new ArrayList<double[][]>(taxa.getTaxonCount());
            for (int i = 0; i < xo.getChildCount(); ++i) {
                final Object cxo = xo.getChild(i);
                if (cxo instanceof XMLObject && ((XMLObject) cxo).getName().equals(SEQUENCE)) {
                    final ArrayList<double[]> sequence = new ArrayList<double[]>(xo.getChildCount());
                    for (int j = 0; j < ((XMLObject) cxo).getChildCount(); ++j) {
                        final Object ccxo = ((XMLObject) cxo).getChild(j);
                        if (ccxo instanceof XMLObject && ((XMLObject) ccxo).getName().equals(PARTIAL))
                            sequence.add(((XMLObject) ccxo).getDoubleArrayAttribute(PROBABILITIES));
                    }
                    alignment.add(sequence.toArray(new double[sequence.size()][]));
                }
            }
            return new PartialsAlignmentModel(taxa, alignment.toArray(new double[alignment.size()][][]));
        }

        private final XMLSyntaxRule[] rules = {
            new ElementRule(SEQUENCE, new XMLSyntaxRule[]{new ElementRule(PARTIAL, new XMLSyntaxRule[]{AttributeRule.newDoubleArrayRule(PROBABILITIES)}, 1, Integer.MAX_VALUE)}, 1, Integer.MAX_VALUE),
            new ElementRule(TaxonList.class)
        };

        @Override
        public XMLSyntaxRule[] getSyntaxRules() {
            return rules;
        }

        @Override
        public String getParserDescription() {
            return "Represents an alignment of partial probability vectors instead of states.";
        }

        @Override
        public Class getReturnType() {
            return PartialsAlignmentModel.class;
        }

        @Override
        public String getParserName() {
            return PARTIALS_ALIGNMENT;
        }
    };
}
