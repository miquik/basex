package org.basex.examples.miki;


import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.value.Value;


public final class Test01 {
	public static void main(final String... args) throws BaseXException, QueryException {
		System.out.println("=== TEST01 ===");

		// final String file = "src/main/resources/csv/example.csv";
		 // string query = "let $a := 12 return $a + $a";
        String query = "let $a := fn:doc(\"src/main/resources/xml/file1.xml\")/root return $a/nome";

        Context context = new Context();
        try (// Create a query processor
        QueryProcessor processor = new QueryProcessor(query, context)) {
            // Execute the query
            Value result = processor.value();

            System.out.println(result);
        }
	}
}
