package com.troblecodings.signals.models;

import java.util.Objects;
import java.util.function.Predicate;

import net.minecraftforge.client.model.data.IModelData;

public class ImplAutoBlockstatePredicate implements Predicate<IModelData> {

    private final int id;

    private static int counter = 0;

    public ImplAutoBlockstatePredicate() {
        this.id = counter++;
    }

    @Override
    public boolean test(final IModelData t) {

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImplAutoBlockstatePredicate other = (ImplAutoBlockstatePredicate) obj;
        return id == other.id;
    }
}
