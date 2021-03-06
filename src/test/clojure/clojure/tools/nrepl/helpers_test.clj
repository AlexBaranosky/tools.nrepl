;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns #^{:doc ""
       :author "Chas Emerick"}
  clojure.tools.nrepl.helpers-test
  (:import (java.io File))
  (:use [clojure.tools.nrepl-test :only (def-repl-test repl-server-fixture)]
    clojure.test)
  (:require
    [clojure.tools.nrepl :as nrepl]
    [clojure.tools.nrepl.helpers :as helpers]))

(def project-base-dir (File. (System/getProperty "nrepl.basedir" ".")))

(use-fixtures :once repl-server-fixture)

(def-repl-test load-code-with-debug-info
  (repl-receive "\n\n\n(defn function [])")
  (is (= {:file "NO_SOURCE_PATH" :line 4}
        (repl-value "(-> #'function meta (select-keys [:file :line]))")))
  
  (repl-receive (helpers/load-file-command
                  "\n\n\n\n\n\n\n\n\n(defn dfunction [])"
                  "path/from/source/root.clj"
                  "root.clj"))
  
  (is (= [{:file "path/from/source/root.clj" :line 10}]
        (nrepl/values-with connection
          (-> #'dfunction
            meta
            (select-keys [:file :line]))))))

(def-repl-test load-file-with-debug-info
  (repl-receive (helpers/load-file-command
                  (File. project-base-dir "load-file-test/clojure/tools/nrepl/load_file_sample.clj")
                  (File. project-base-dir "load-file-test")))
  (repl-receive (helpers/load-file-command
                  (.getAbsolutePath (File. project-base-dir "load-file-test/clojure/tools/nrepl/load_file_sample.clj"))
                  (File. project-base-dir "load-file-test")))
  (is (= [{:file "clojure/tools/nrepl/load_file_sample.clj" :line 5}]
        (nrepl/values-with connection
          (-> #'clojure.tools.nrepl.load-file-sample/dfunction
            meta
            (select-keys [:file :line]))))))
