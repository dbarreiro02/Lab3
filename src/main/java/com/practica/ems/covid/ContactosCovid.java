package com.practica.ems.covid;


import com.practica.excecption.*;
import com.practica.genericas.*;
import com.practica.lista.ListaContactos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static com.practica.ems.covid.Localizacion.parsearHoraAux;

public class ContactosCovid {
    private Poblacion poblacion;
    private Localizacion localizacion;
    private ListaContactos listaContactos;

    public ContactosCovid() {
        this.poblacion = new Poblacion();
        this.localizacion = new Localizacion();
        this.listaContactos = new ListaContactos();
    }

    public Poblacion getPoblacion() {
        return poblacion;
    }

    public Localizacion getLocalizacion() {
        return localizacion;
    }

    public void setLocalizacion(Localizacion localizacion) {
        this.localizacion = localizacion;
    }


    public ListaContactos getListaContactos() {
        return listaContactos;
    }

    public void loadData(String data, boolean reset) throws EmsInvalidTypeException, EmsInvalidNumberOfDataException,
            EmsDuplicatePersonException, EmsDuplicateLocationException {
        // borro información anterior
        if (reset) {
            this.poblacion = new Poblacion();
            this.localizacion = new Localizacion();
            this.listaContactos = new ListaContactos();
        }
        String[] datas = dividirEntrada(data);
        leerFicheroPersonaLocalizacion(datas);
    }

    public void loadDataFile(String fichero, boolean reset) {
        loadDataFile(fichero, reset, null);
    }

    @SuppressWarnings("resource")
    public void loadDataFile(String fichero, boolean reset, FileReader fr) {
        try {
            // Apertura del fichero y creacion de BufferedReader para poder
            // hacer una lectura comoda (disponer del metodo readLine()).
            File archivo = new File(fichero);
            fr = new FileReader(archivo);
            BufferedReader br = new BufferedReader(fr);
            if (reset) {
                this.poblacion = new Poblacion();
                this.localizacion = new Localizacion();
                this.listaContactos = new ListaContactos();
            }
            /*
             * Lectura del fichero	línea a línea. Compruebo que cada línea
             * tiene el tipo PERSONA o LOCALIZACION y cargo la línea de datos en la
             * lista correspondiente. Sino viene ninguno de esos tipos lanzo una excepción
             */
            String data;
            while ((data = br.readLine()) != null) {
                String[] datas = dividirEntrada(data.trim());
                leerFicheroPersonaLocalizacion(datas);

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // En el finally cerramos el fichero, para asegurarnos
            // que se cierra tanto si todo va bien como si salta
            // una excepcion.
            try {
                if (null != fr) {
                    fr.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void leerFicheroPersonaLocalizacion(String[] datas) throws EmsInvalidTypeException, EmsInvalidNumberOfDataException, EmsDuplicatePersonException, EmsDuplicateLocationException {
        for (String linea : datas) {
            String[] datos = this.dividirLineaData(linea);
            if (!datos[0].equals("PERSONA") && !datos[0].equals("LOCALIZACION")) {
                throw new EmsInvalidTypeException();
            }
            if (datos[0].equals("PERSONA")) {
                if (datos.length != Constantes.MAX_DATOS_PERSONA) {
                    throw new EmsInvalidNumberOfDataException("El número de datos para PERSONA es menor de 8");
                }
                this.poblacion.addPersona(this.crearPersona(datos));
            }
            if (datos[0].equals("LOCALIZACION")) {
                if (datos.length != Constantes.MAX_DATOS_LOCALIZACION) {
                    throw new EmsInvalidNumberOfDataException(
                            "El número de datos para LOCALIZACION es menor de 6");
                }
                PosicionPersona pp = this.crearPosicionPersona(datos);
                this.localizacion.addLocalizacion(pp);
                this.listaContactos.insertarNodoTemporal(pp);
            }
        }
    }

    public int findPersona(String documento) throws EmsPersonNotFoundException {
        int pos;
        try {
            pos = this.poblacion.findPersona(documento);
            return pos;
        } catch (EmsPersonNotFoundException e) {
            throw new EmsPersonNotFoundException();
        }
    }

    public int findLocalizacion(String documento, String fecha, String hora) throws EmsLocalizationNotFoundException {

        int pos;
        try {
            pos = localizacion.findLocalizacion(documento, fecha, hora);
            return pos;
        } catch (EmsLocalizationNotFoundException e) {
            throw new EmsLocalizationNotFoundException();
        }
    }

    public List<PosicionPersona> localizacionPersona(String documento) throws EmsPersonNotFoundException {
        int cont = 0;
        List<PosicionPersona> lista = new ArrayList<>();
        for (PosicionPersona pp : this.localizacion.getLista()) {
            if (pp.getDocumento().equals(documento)) {
                cont++;
                lista.add(pp);
            }
        }
        if (cont == 0)
            throw new EmsPersonNotFoundException();
        else
            return lista;
    }

    public void delPersona(String documento) throws EmsPersonNotFoundException {
        int cont = 0, pos = -1;
        for (Persona persona : this.poblacion.getLista()) {
            if (persona.getDocumento().equals(documento)) {
                pos = cont;
            }
            cont++;
        }
        if (pos == -1) {
            throw new EmsPersonNotFoundException();
        }
        this.poblacion.getLista().remove(pos);
    }

    private String[] dividirEntrada(String input) {
        return input.split("\\n");
    }

    private String[] dividirLineaData(String data) {
        return data.split(";");
    }

    private Persona crearPersona(String[] data) {
        Persona persona = new Persona();
        for (int i = 1; i < Constantes.MAX_DATOS_PERSONA; i++) {
            String s = data[i];
            switch (i) {
                case 1:
                    persona.setDocumento(s);
                    break;
                case 2:
                    persona.setNombre(s);
                    break;
                case 3:
                    persona.setApellidos(s);
                    break;
                case 4:
                    persona.setEmail(s);
                    break;
                case 5:
                    persona.setDireccion(s);
                    break;
                case 6:
                    persona.setCp(s);
                    break;
                case 7:
                    persona.setFechaNacimiento(parsearFecha(s));
                    break;
            }
        }
        return persona;
    }

    private PosicionPersona crearPosicionPersona(String[] data) {
        PosicionPersona posicionPersona = new PosicionPersona();
        String fecha = null, hora;
        float latitud = 0, longitud;
        for (int i = 1; i < Constantes.MAX_DATOS_LOCALIZACION; i++) {
            String s = data[i];
            switch (i) {
                case 1:
                    posicionPersona.setDocumento(s);
                    break;
                case 2:
                    fecha = data[i];
                    break;
                case 3:
                    hora = data[i];
                    assert fecha != null;
                    posicionPersona.setFechaPosicion(parsearFecha(fecha, hora));
                    break;
                case 4:
                    latitud = Float.parseFloat(s);
                    break;
                case 5:
                    longitud = Float.parseFloat(s);
                    posicionPersona.setCoordenada(new Coordenada(latitud, longitud));
                    break;
            }
        }
        return posicionPersona;
    }

    private FechaHora parsearFecha(String fecha) {
        return getFechaHora(fecha);
    }

    static FechaHora getFechaHora(String fecha) {
        int dia, mes, anio;
        String[] valores = fecha.split("/");
        dia = Integer.parseInt(valores[0]);
        mes = Integer.parseInt(valores[1]);
        anio = Integer.parseInt(valores[2]);
        return new FechaHora(dia, mes, anio, 0, 0);
    }

    private FechaHora parsearFecha(String fecha, String hora) {
        return parsearHoraAux(fecha, hora);
    }
}
