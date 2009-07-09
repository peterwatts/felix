/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.karaf.gshell.packages;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.apache.felix.gogo.commands.Argument;

public class ImportsCommand extends PackageCommandSupport {

    @Argument(required = false, multiValued = true, description = "bundle ids")
    List<Long> ids;

    protected void doExecute(PackageAdmin admin) throws Exception {
        Map<Long, List<ExportedPackage>> packages = new HashMap<Long, List<ExportedPackage>>();
        ExportedPackage[] exported = admin.getExportedPackages((Bundle) null);
        for (ExportedPackage pkg : exported) {
            Bundle[] bundles = pkg.getImportingBundles();
            if (bundles != null) {
                for (Bundle b : bundles) {
                    List<ExportedPackage> p = packages.get(b.getBundleId());
                    if (p == null) {
                        p = new ArrayList<ExportedPackage>();
                        packages.put(b.getBundleId(), p);
                    }
                    p.add(pkg);
                }
            }
        }
        if (ids != null && !ids.isEmpty()) {
            for (long id : ids) {
                Bundle bundle = getBundleContext().getBundle(id);
                if (bundle != null) {
                    printImports(System.out, bundle, packages.get(bundle.getBundleId()));
                } else {
                    System.err.println("Bundle ID " + id + " is invalid.");
                }
            }
        }
        else {
            List<ExportedPackage> pkgs = new ArrayList<ExportedPackage>();
            for (List<ExportedPackage> l : packages.values()) {
                pkgs.addAll(l);
            }
            printImports(System.out, null, pkgs);
        }
    }

    protected void printImports(PrintStream out, Bundle target, List<ExportedPackage> imports) {
        if ((imports != null) && (imports.size() > 0)) {
            for (ExportedPackage p : imports) {
                Bundle bundle = p.getExportingBundle();
                out.print(getBundleName(bundle));
                out.println(": " + p);
            }
        } else {
            out.println(getBundleName(target) + ": No active imported packages.");
        }
    }

    public static String getBundleName(Bundle bundle) {
        if (bundle != null) {
            String name = (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);
            return (name == null)
                ? "Bundle " + Long.toString(bundle.getBundleId())
                : name + " (" + Long.toString(bundle.getBundleId()) + ")";
        }
        return "[STALE BUNDLE]";
    }

}
