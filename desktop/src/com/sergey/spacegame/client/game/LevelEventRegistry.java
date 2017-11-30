package com.sergey.spacegame.client.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sergey.spacegame.client.event.AtlasRegistryEvent;
import com.sergey.spacegame.client.event.LocalizationRegistryEvent;
import com.sergey.spacegame.common.event.EventHandle;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * This class represents the an event listener for registry events needed for a level
 *
 * @author sergeys
 */
public class LevelEventRegistry {
    
    //The filesystem of the level file
    private FileSystem fileSystem;
    
    /**
     * Create a new LevelEventRegistry
     *
     * @param fileSystem - the filesystem of the level's sgl file
     */
    public LevelEventRegistry(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    
    /**
     * Register images contained by this level into the atlas
     *
     * @param event - the event that caused this handler to fire
     */
    @EventHandle
    public void onAtlasRegistry(AtlasRegistryEvent event) {
        try {
            //Walk the images directoy and add all of those images to the registry
            Path images = fileSystem.getPath("images");
            if (Files.exists(images)) {
                Files.walkFileTree(images, new ImageFileWalker(event.getPacker()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Register localization information needed for the level
     *
     * @param event - the event that caused this handler to fire
     */
    @EventHandle
    public void onLocalizationRegistry(LocalizationRegistryEvent event) {
        try {
            //Register each localization for the given locale
            Files.lines(fileSystem.getPath("localization", event.getLocale() + ".loc"))
                    .filter(s -> !s.startsWith("#") && s.matches("([^=]+)\\s*=([^=]+)?"))
                    .forEach(s -> {
                        String[] parts = s.split("\\s*=");
                        event.getLocalizationMap().put(parts[0], parts.length > 1 ? parts[1] : "");
                    });
        } catch (IOException e) {
            System.out.println("Localization file not found: " + event.getLocale());
        }
    }
    
    private static class ImageFileWalker implements FileVisitor<Path> {
        
        private PixmapPacker packer;
        
        public ImageFileWalker(PixmapPacker packer) {
            this.packer = packer;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!Files.isHidden(file) && Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
                try {
                    String name = file.toString()
                            .substring(0, file.toString().lastIndexOf('.'))
                            .replaceFirst("/images/", "");
                    byte[] bytes = Files.readAllBytes(file);
                    packer.pack(name, new Pixmap(bytes, 0, bytes.length));
                } catch (GdxRuntimeException e) {
                    System.err.println("Failed to load file: " + file + " as an image.");
                }
            }
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            System.err.println("ERROR: Cannot visit path: " + file);
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}

