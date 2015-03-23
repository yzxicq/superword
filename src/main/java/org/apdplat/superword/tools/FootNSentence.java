/**
 *
 * APDPlat - Application Product Development Platform Copyright (c) 2013, 杨尚川,
 * yang-shangchuan@qq.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.apdplat.superword.tools;

import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 挑选分值最低的N个句子
 * @author 杨尚川
 */
public class FootNSentence {
    public static TreeMap<Float, String> footN(String path, int limit) {
        return TextAnalyzer.sentence(path, limit, false);
    }
    public static void main(String[] args) throws Exception {
        TreeMap<Float, String> data = footN("src/main/resources/it", 100);
        AtomicInteger i = new AtomicInteger();
        data.keySet().forEach(k -> System.out.println(i.incrementAndGet()+"\t"+k+"\n"+data.get(k)));
    }
}
