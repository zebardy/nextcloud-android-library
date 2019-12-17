/*
 *
 * Nextcloud Android client application
 *
 * @author Tobias Kaminsky
 * Copyright (C) 2019 Tobias Kaminsky
 * Copyright (C) 2019 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.nextcloud.android.lib.resources.directediting;

import com.owncloud.android.AbstractIT;
import com.owncloud.android.lib.common.DirectEditing;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DirectEditingObtainRemoteOperationTest extends AbstractIT {

    @Test
    public void testGetAll() {
        RemoteOperationResult result = new DirectEditingObtainRemoteOperation().execute(client);
        assertTrue(result.isSuccess());

        DirectEditing directEditing = (DirectEditing) result.getSingleData();

        assertEquals("Nextcloud Text", directEditing.editors.get("text").name);
        assertEquals("text/plain", directEditing.editors.get("text").optionalMimetypes.get(0));

        assertEquals("New text document", directEditing.creators.get("textdocument").name);
        assertFalse(directEditing.creators.get("textdocument").templates);
    }
}
