package me.zoxouu.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.zoxouu.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config {
    private File file;

    private Config instance = null;


    public void loadInstance(String fileName, String folderName, Config instance) {
        this.file = new File(folderName+"/"+fileName+".json");;
        this.instance = instance;
        load();
    }


    public void save() {
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
        try {
            for (Field field : getConfigFields()) {
                field.setAccessible(true);
                fieldMap.put(field.getName(), field.get(this));
            }

            IOUtils.save(prettyGson.toJson(fieldMap), file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Field[] getConfigFields() {
        if(instance == null) {
            throw new NullPointerException("You need to define the class fields using setInstance() in your config.");
        }
        return Arrays.stream(instance.getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(ConfigField.class)).toArray(Field[]::new);
    }

    private void load() {
        if (!file.exists()) {
            System.out.println("No config detected, running on default config");
            try {
                boolean isCreated = IOUtils.create(file);
                if (!isCreated) {
                    System.out.println("Can't create the default config file, am I missing some permissions ?");
                } else {
                    save();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                String json = IOUtils.readFileToString(file.toPath());
                JsonObject fields = new Gson().fromJson(json, JsonObject.class);
                if(fields == null) {
                    System.out.println("Empty config detected ! ("+file.getName()+") Using default one.");
                    return;
                }
                boolean needSave = false;
                for (Field field : getConfigFields()) {
                    String fieldName = field.getName();

                    if (fields.has(fieldName)) {

                        JsonElement jsonElement = fields.get(fieldName);
                        Object value = new Gson().fromJson(jsonElement, field.getType());

                        field.setAccessible(true);
                        field.set(this, value);
                    } else {
                        needSave = true;
                    }
                }
                if(needSave) save();
            } catch (IOException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}