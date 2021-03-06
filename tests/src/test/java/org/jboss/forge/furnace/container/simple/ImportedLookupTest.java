/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.furnace.container.simple;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.arquillian.services.LocalServices;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.furnace.container.simple.services.MockService;
import org.jboss.forge.furnace.container.simple.services.MockServiceConsumer;
import org.jboss.forge.furnace.container.simple.services.MockServicePayload;
import org.jboss.forge.furnace.exception.ContainerException;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ImportedLookupTest
{
   @Deployment(order = 3)
   @Dependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:simple")
   })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addClasses(MockService.class, MockServiceConsumer.class)
               .addAsLocalServices(ImportedLookupTest.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple")
               );

      return archive;
   }

   @Deployment(name = "dep1,1", testable = false, order = 2)
   public static ForgeArchive getDeploymentDep1()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addClasses(MockServiceConsumer.class, MockService.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("dep3"),
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple")
               );

      return archive;
   }

   @Deployment(name = "dep3,1", testable = false, order = 0)
   public static ForgeArchive getDeploymentDep3()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addClasses(MockServicePayload.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:simple")
               );

      return archive;
   }

   @Test(expected = ContainerException.class)
   public void testDoesNotResolveNonService() throws Exception
   {
      AddonRegistry registry = LocalServices.getFurnace(getClass().getClassLoader())
               .getAddonRegistry();

      Imported<MockServiceConsumer> importedByName = registry.getServices(MockServiceConsumer.class.getName());
      Assert.assertTrue(importedByName.isUnsatisfied());
      importedByName.get();
   }

}
