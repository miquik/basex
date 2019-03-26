package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;

import java.util.concurrent.*;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author James Wright
 */
public final class XQueryForkJoin extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value funcs = exprs[0].value(qc);
    for(final Item func : funcs) {
      if(!(func instanceof FItem) || ((FItem) func).arity() != 0)
        throw ZEROFUNCS_X_X.get(info, func.type, func);
    }
    // no functions specified: return empty sequence
    if(funcs.isEmpty()) return Empty.VALUE;
    // single function: invoke directly
    if(funcs instanceof Item) return ((FItem) funcs).invokeValue(qc, info);

    final ForkJoinPool pool = new ForkJoinPool();
    final XQueryTask task = new XQueryTask(funcs, qc, info);
    try {
      return pool.invoke(task);
    } catch(final Exception ex) {
      // pass on query and job exceptions
      final Throwable e = Util.rootException(ex);
      if(e instanceof QueryException) throw (QueryException) e;
      if(e instanceof JobException) throw (JobException) e;
      throw XQUERY_UNEXPECTED_X.get(info, e);
    } finally {
      // required?
      pool.shutdown();
    }
  }
}
