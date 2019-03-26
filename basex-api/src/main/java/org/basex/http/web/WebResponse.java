package org.basex.http.web;

import java.io.*;

import javax.servlet.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.util.*;

/**
 * This abstract class defines common methods of Web responses.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class WebResponse {
  /** Database context. */
  protected final Context ctx;
  /** Query context. */
  protected QueryContext qc;

  /**
   * Constructor.
   * @param ctx database context
   */
  protected WebResponse(final Context ctx) {
    this.ctx = ctx;
  }

  /**
   * Creates the Response.
   * @param function function to be evaluated
   * @param data additional data (result, function, error, can be {@code null})
   * @return result flag (ignored)
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   * @throws ServletException servlet exception
   */
  public boolean create(final WebFunction function, final Object data)
      throws QueryException, IOException, ServletException {

    final WebModule module = function.module;
    final StaticFunc func = function.function;
    try {
      qc = module.qc(ctx);
      init(function);
      final StaticFunc sf = WebModule.find(qc, func);
      final Expr[] args = new Expr[sf.params.length];
      bind(args, data);
      qc.jc().description(toString(func, args));
      qc.mainModule(MainModule.get(sf, args));
      return serialize();
    } catch(final QueryException ex) {
      if(ex.file() == null) ex.info(func.info);
      throw ex;
    } finally {
      if(qc != null) qc.close();
    }
  }

  /**
   * Creates and returns a function instance that can be evaluated.
   * @param function function template
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void init(WebFunction function) throws QueryException, IOException;

  /**
   * Binds values to the function parameters.
   * @param args arguments
   * @param data additional data (result, function, error, can be {@code null})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void bind(Expr[] args, Object data) throws QueryException, IOException;

  /**
   * Serializes the response.
   * @return {@code true} if data was serialized
   * @throws QueryException query exception
   * @throws IOException I/O exception
   * @throws ServletException servlet exception
   */
  protected abstract boolean serialize() throws QueryException, IOException, ServletException;

  /**
   * Returns a job description for the specified function.
   * @param func function
   * @param args arguments
   * @return description
   */
  private static String toString(final StaticFunc func, final Expr[] args) {
    final TokenBuilder tb = new TokenBuilder().add(func.info).add(Text.COLS);
    tb.add(func.name.prefixString()).add('(');
    final int al = args.length;
    for(int a = 0; a < al; a++) {
      if(a != 0) tb.add(", ");
      tb.add(func.params[a].toErrorString()).add(" := ").add(args[a]);
    }
    return tb.add(')').toString();
  }
}
