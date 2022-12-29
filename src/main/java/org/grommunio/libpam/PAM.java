/*
 * The MIT License
 *
 * Copyright (c) 2009, Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.grommunio.libpam;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.jboss.logging.Logger;
import org.grommunio.libpam.impl.CLibrary.passwd;
import org.grommunio.libpam.impl.PAMLibrary.pam_conv;
import org.grommunio.libpam.impl.PAMLibrary.pam_conv.PamCallback;
import org.grommunio.libpam.impl.PAMLibrary.pam_handle_t;
import org.grommunio.libpam.impl.PAMLibrary.pam_message;
import org.grommunio.libpam.impl.PAMLibrary.pam_response;

import java.util.Set;

import static com.sun.jna.Native.POINTER_SIZE;
import static org.grommunio.libpam.impl.CLibrary.libc;
import static org.grommunio.libpam.impl.PAMLibrary.PAM_CONV_ERR;
import static org.grommunio.libpam.impl.PAMLibrary.PAM_PROMPT_ECHO_OFF;
import static org.grommunio.libpam.impl.PAMLibrary.PAM_SUCCESS;
import static org.grommunio.libpam.impl.PAMLibrary.PAM_USER;
import static org.grommunio.libpam.impl.PAMLibrary.libpam;

/**
 * PAM authenticator.
 * <p>
 * <p>
 * Instances are thread unsafe and non reentrant. An instace cannot be reused
 * to authenticate multiple users.
 * <p>
 * <p>
 * For an overview of PAM programming, refer to the following resources:
 * <p>
 * <ul>
 * <li><a href="http://www.netbsd.org/docs/guide/en/chap-pam.html">NetBSD PAM programming guide</a>
 * <li><a href="http://www.kernel.org/pub/linux/libs/pam/">Linux PAM</a>
 * </ul>
 *
 * @author Kohsuke Kawaguchi
 */
public class PAM {
    private pam_handle_t pht;
    private int ret;

    /**
     * Temporarily stored to pass a value from {@link #authenticate(String, String...)}
     * to {@link pam_conv}.
     */
    private String[] factors;

    /**
     * Creates a new authenticator.
     *
     * @param serviceName PAM service name. This corresponds to the service name that shows up
     *                    in the PAM configuration,
     */
    public PAM(String serviceName) throws PAMException {
        pam_conv conv = new pam_conv(new PamCallback() {
            public int callback(int num_msg, Pointer msg, Pointer resp, Pointer _ptr) {
                LOGGER.info("pam_conv num_msg=" + num_msg);
                if (factors == null)
                    return PAM_CONV_ERR;

                // allocates pam_response[num_msg]. the caller will free this
                Pointer m = libc.calloc(pam_response.SIZE, num_msg);
                resp.setPointer(0, m);

                for (int i = 0; i < factors.length; i++) {
                    pam_message pm = new pam_message(msg.getPointer(POINTER_SIZE * i));
                    LOGGER.info(pm.msg_style + ":" + pm.msg);
                    if (pm.msg_style == PAM_PROMPT_ECHO_OFF) {
                        pam_response r = new pam_response(m.share(pam_response.SIZE * i));
                        r.setResp(factors[i]);
                        r.write(); // write to (*resp)[i]
                    }
                }

                return PAM_SUCCESS;
            }
        });

        PointerByReference phtr = new PointerByReference();
        check(libpam.pam_start(serviceName, null, conv, phtr), "pam_start failed");
        pht = new pam_handle_t(phtr.getValue());
    }

    private void check(int ret, String msg) throws PAMException {
        this.ret = ret;
        if (ret != 0) {
            if (pht != null)
                throw new PAMException(msg + " : " + libpam.pam_strerror(pht, ret));
            else
                throw new PAMException(msg);
        }
    }

    /**
     * Authenticate the user with a password.
     *
     * @return Upon a successful authentication, return true.
     * @throws PAMException If the authentication fails.
     */
    public boolean authenticate(String username, String... factors) throws PAMException {
        this.factors = factors;
	boolean ret = false;
        try {
            check(libpam.pam_set_item(pht, PAM_USER, username), "pam_set_item failed");
            check(libpam.pam_authenticate(pht, 0), "pam_authenticate failed");
            check(libpam.pam_setcred(pht, 0), "pam_setcred failed");
            // several different error code seem to be used to represent authentication failures
            check(libpam.pam_acct_mgmt(pht,0),"pam_acct_mgmt failed");

            ret = true;
        } finally {
            this.factors = null;
	    return ret;
        }
    }

    /**
     * After a successful authentication, call this method to obtain the effective user name.
     * This can be different from the user name that you passed to the {@link #authenticate(String, String)}
     * method.
     */

    /**
     * Performs an early disposal of the object, instead of letting this GC-ed.
     * Since PAM may hold on to native resources that don't put pressure on Java GC,
     * doing this is a good idea.
     * <p>
     * <p>
     * This method is called by {@link #finalize()}, too, so it's not required
     * to call this method explicitly, however.
     */
    public void dispose() {
        if (pht != null) {
            libpam.pam_end(pht, ret);
            pht = null;
        }
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    private static final Logger LOGGER = Logger.getLogger(PAM.class.getName());
}
