/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) 2022 - 2022-2026 TerrorByte and Honeypot Contributors.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

package org.reprogle.honeypot.common.storageproviders;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("org.reprogle.honeypot.common.storageproviders.HoneypotStore")
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public final class HoneypotStoreProcessor extends AbstractProcessor {
    @Override
    public boolean process(
        Set<? extends TypeElement> annotations,
        RoundEnvironment roundEnv
    ) {
        for (Element element : roundEnv.getElementsAnnotatedWith(HoneypotStore.class)) {
            if (!(element instanceof TypeElement typeElement)) {
                continue;
            }

            HoneypotStore store = typeElement.getAnnotation(HoneypotStore.class);

            for (StoreType type : store.type()) {
                switch (type) {
                    case REGION -> requireInterface(
                        typeElement,
                        "org.reprogle.honeypot.common.storageproviders.RegionStore",
                        type);
                    case PLAYER -> requireInterface(
                        typeElement,
                        "org.reprogle.honeypot.common.storageproviders.PlayerStore",
                        type);
                    case PLAYER_HISTORY -> requireInterface(
                        typeElement,
                        "org.reprogle.honeypot.common.storageproviders.PlayerHistoryStore",
                        type);
                }
            }
        }

        return true;
    }

    private void requireInterface(
        TypeElement classElement,
        String requiredInterfaceName,
        StoreType storeType
    ) {
        TypeElement requiredInterface =
            processingEnv.getElementUtils()
                .getTypeElement(requiredInterfaceName);

        if (requiredInterface == null) {
            error(
                "Could not resolve required interface: " + requiredInterfaceName,
                classElement
            );
            return;
        }

        TypeMirror classType = classElement.asType();
        TypeMirror interfaceType = requiredInterface.asType();

        if (!processingEnv.getTypeUtils().isAssignable(classType, interfaceType)) {
            error(
                "@HoneypotStore(type = " + storeType + ") requires "
                    + classElement.getSimpleName()
                    + " to implement "
                    + requiredInterface.getSimpleName(),
                classElement
            );
        }
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            message,
            element
        );
    }
}
