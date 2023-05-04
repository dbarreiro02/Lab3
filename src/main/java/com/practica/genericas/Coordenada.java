package com.practica.genericas;


public class Coordenada {
    private float latitud, longitud;


    public Coordenada(float latitud, float longitud) {
        super();
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public float getLatitud() {
        return latitud;
    }

    public float getLongitud() {
        return longitud;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int aux = prime + Float.floatToIntBits(latitud);
        return prime * aux + Float.floatToIntBits(longitud);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Coordenada other = (Coordenada) obj;
        if (Float.floatToIntBits(latitud) != Float.floatToIntBits(other.latitud))
            return false;
        return Float.floatToIntBits(longitud) == Float.floatToIntBits(other.longitud);
    }

}
