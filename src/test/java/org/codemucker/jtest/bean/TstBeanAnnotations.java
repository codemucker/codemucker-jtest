/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codemucker.jtest.bean;

import org.codemucker.jpattern.Property;

public class TstBeanAnnotations {

	private String myField;

	@Property(name = "noMethods")
	private String myFieldNoMethods;

	@Property(name = "  ")
	private String myFieldEmptyAnnotationName;

	@Property(name = "customName")
	public String getMyField() {
		return myField;
	}

	@Property(name = "customName")
	public void setMyField(String myField) {
		this.myField = myField;
	}

}
