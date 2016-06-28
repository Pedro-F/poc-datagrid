/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dataGridSrv1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Gencur
 */
public class Prenda implements Serializable {

    private static final long serialVersionUID = -181403229462007401L;

    private String prendaName;
    private List<String> colores;

    public Prenda(String prendaName) {
        this.prendaName = prendaName;
        colores = new ArrayList<String>();
    }

    public void addColor(String color) {
        colores.add(color);
    }

    public void removeColor(String color) {
        colores.remove(color);
    }

    public List<String> getColores() {
        return colores;
    }

    public String getPrendaName() {
        return prendaName;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("=== Prenda: " + prendaName + " ===\n");
        b.append("Colores:\n");
        for (String color : colores) {
            b.append("- " + color + "\n");
        }
        return b.toString();
    }
    public boolean equals(Prenda other)
    {
    	return this.getPrendaName().equals(other.getPrendaName());
    }
}