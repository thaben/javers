package org.javers.core.json.typeadapter.commit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.DiffBuilder;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class DiffTypeDeserializer implements JsonDeserializer<Diff> {
    private static final String CHANGES_FIELD = "changes";

    @Override
    public Diff deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonObject)) {
            return null; //when user's class is refactored, a property can have changed type
        }
        JsonObject jsonObject = (JsonObject) json;

        if (jsonObject.get(CHANGES_FIELD) != null) {
            List<Change> changes = context.deserialize(jsonObject.get(CHANGES_FIELD), new TypeToken<List<Change>>(){}.getType());
            return new DiffBuilder()
                    .addChanges(changes, Optional.empty())
                    .build();
        }
        return null;
    }

}
