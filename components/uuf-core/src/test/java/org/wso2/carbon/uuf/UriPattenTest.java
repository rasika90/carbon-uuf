/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.uuf;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.uuf.internal.core.UriPatten;

import static java.lang.Integer.signum;

public class UriPattenTest {
    @DataProvider
    public Object[][] invalidUriPatterns() {
        return new Object[][]{
                {"", "URI pattern cannot be empty."},
                {"a", "URI patten must start with a '/'."},
                {"/{", "at index 1"},
                {"/{a", "at index 1"},
                {"/{{abc", "at index 2"},
                {"/}", "at index 1"},
                {"/a}", "at index 2"},
                {"/a}}", "at index 2"},
                {"/{a}{", "at index 4"},
                {"/{a}}", "at index 4"},
                {"/{ab{c}}", "at index 4"},
                {"/{+a}}", "at index 5"},
                {"/{+a}{", "at index 5"},
                {"/{+ab{c}}", "at index 5"},
                {"/{+a}/", "at index 5"}
        };
    }

    @DataProvider
    public Object[][] matchingUriPatterns() {
        return new Object[][]{
                {"/", "/"},
                {"/a", "/a"},
                {"/-._~?#[]@!$&'()+,;=", "/-._~?#[]@!$&'()+,;="},
                {"/{x}", "/a"},
                {"/{x}", "/-._~?#[]@!$&'()+,;="},
                {"/a{x}", "/ab"},
                {"/{x}b", "/ab"},
                {"/a/{x}", "/a/b"},
                {"/a{x}c/d{y}f", "/abc/def"},
                {"/{+x}", "/a"},
                {"/{+x}", "/-._~?#[]@!$&'()+,;="},
                {"/{+x}", "/a/"},
                {"/{+x}", "/a/b/c"},
                {"/a/{x}/{+y}", "/a/b/c/d"},
                {"/a/{x}/c/de{+y}", "/a/b/c/def/g/"},
                {"/index", "/"},
                {"/a/index", "/a"},
                {"/a/{x}/index", "/a/b"}
        };
    }

    @DataProvider
    public Object[][] unmatchingUriPatterns() {
        return new Object[][]{
                {"/", "/a"},
                {"/a", "/abc"},
                {"/a/", "/a"},
                {"/{x}", "/a/b"},
                {"/{x}/", "/a/b"},
                {"/a/b/{x}", "/a/b/c/d"},
                {"/a{+x}", "/A/b"},
                {"/a/{+x}", "/A/b"},
                {"/a/b{+x}", "/a/Bc/d"},
                {"/index", "/index"},
                {"/a/index", "/a/index"},
                {"/a/{x}/index", "/a/b/index"}
        };
    }

    @DataProvider
    public Object[][] orderedUriPatterns() {
        return new Object[][]{
                {"/a", "/{a}"},
                {"/a/b", "/{a}/b"},
                {"/a/b", "/a/{b}"},
                {"/a/b/", "/a/{b}/"},
                {"/a/b", "/{a}/{b}"},
                {"/a/b/", "/{a}/{b}/"},
                {"/{a}/b", "/{a}/{b}"},
                {"/a/{b}", "/{a}/{b}"},
                {"/ab", "/a{b}"},
                {"/ab", "/{a}b"}
        };
    }

    @Test(dataProvider = "invalidUriPatterns")
    public void testInvalidUriPatterns(String uriPattern, String message) {
        try {
            new UriPatten(uriPattern);
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains(message));
        }
    }

    @Test(dataProvider = "matchingUriPatterns")
    public void testMatching(String uriPattern, String uri) {
        Assert.assertTrue(new UriPatten(uriPattern).matches(uri));
    }

    @Test(dataProvider = "unmatchingUriPatterns")
    public void testUnmatching(String uriPattern, String uri) {
        Assert.assertFalse(new UriPatten(uriPattern).matches(uri));
    }

    @Test(dataProvider = "orderedUriPatterns")
    public void testOrdering(String a, String b) throws Exception {
        UriPatten aPatten = new UriPatten(a);
        UriPatten bPatten = new UriPatten(b);
        int i = aPatten.compareTo(bPatten);
        int j = bPatten.compareTo(aPatten);
        Assert.assertTrue(i < 0, a + " should be more specific than " + b);
        Assert.assertTrue(j > 0, a + " should be more specific than " + b);
    }

    @Test
    public void testInvariants() throws Exception {
        UriPatten[] pattens = new UriPatten[]{
                new UriPatten("/"),
                new UriPatten("/a"),
                new UriPatten("/ab"),
                new UriPatten("/a{b}"),
                new UriPatten("/{a}/{b}"),
                new UriPatten("/{a}"),
                new UriPatten("/{a}"),
                new UriPatten("/a/{b}"),
                new UriPatten("/ab/{b}"),
                new UriPatten("/{a}/b"),
        };
        //following invariants are specified in java.lang.Comparable
        for (int x = 0; x < pattens.length; x++) {
            for (int y = x + 1; y < pattens.length; y++) {
                int xy = pattens[x].compareTo(pattens[y]);
                int yx = pattens[y].compareTo(pattens[x]);
                Assert.assertTrue(signum(xy) == -signum(yx));
                for (int z = y + 1; z < pattens.length; z++) {
                    int xz = pattens[x].compareTo(pattens[z]);
                    int yz = pattens[y].compareTo(pattens[z]);
                    if (xy > 0 && yz > 0) {
                        Assert.assertTrue(xz > 0);
                    } else if (yx > 0 && yz > 0) {
                        Assert.assertTrue(yz > 0);
                    } else if (xy == 0) {
                        Assert.assertTrue(signum(xz) == signum(yz));
                    }
                }
            }
        }
    }
}
