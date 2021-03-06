(ns 
  ^{:author "mikera"
    :doc "Core clisk image generation functions"}
  clisk.core
  (:import clisk.Util)
  (:import [java.awt.image BufferedImage])
  (:import [mikera.gui Frames])
  (:import [javax.swing JComponent])
  (:use [mikera.cljutils core])
  (:require [clojure test])
  (:require [mikera.image.core :as imagez])
  (:use [clisk node functions util]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(def ^:dynamic *anti-alias* 2)

(defn sample 
  "Samples the value of a node at a given position"
  ([node] (sample node [0.0 0.0]))
  ([node pos]
    (let [pos (vectorize pos)
          node (vectorize node)
          fns (vec (map compile-fn (:nodes node)))
          [x y z t] (map #(evaluate (component % pos)) (range 4))]
      (vec 
        (map #(.calc ^clisk.IFunction % (double x) (double y) (double z) (double t))
             fns)))))


(defn tst [] (clojure.test/run-all-tests))

(defn scale-image 
  "Scales an image to a given width and height"
  (^BufferedImage [^BufferedImage img w h]
    (imagez/scale-image img w h)))

(defn show-comp 
  "Shows a component in a new frame"
  ([com 
    & {:keys [^String title]
       :as options
       :or {title nil}}]
  (let [^JComponent  com (component com)]
    (Frames/display com title))))

(defn vector-function 
  "Defines a vector function, operating on vectorz vectors"
  (^clisk.VectorFunction [a 
                & {:keys [input-dimensions]}]
    (let [a (vectorize a)
          input-dimensions (int (or input-dimensions 4))
          ^java.util.List funcs (vec (map compile-fn (:nodes a)))]
      (clisk.VectorFunction/create input-dimensions funcs))))

(defn image
  "Creates a bufferedimage from the given clisk data"
  ([vector-function
    & {:keys [width height size anti-alias] 
       :or {size DEFAULT-IMAGE-SIZE}}]
    (let [vector-function (validate (node vector-function))
          scale (or anti-alias *anti-alias*)
          w (int (or width size))
          h (int (or height size))
          fw (* w scale)
          fh (* h scale)
          img (img vector-function fw fh)]
      (scale-image img w h))))

(defn show 
  "Creates and shows an image from the given vector function"
  ([vector-function
    & {:keys [width height size anti-alias] 
       :or {size DEFAULT-IMAGE-SIZE}
       :as keys}]
    (Util/show ^BufferedImage (apply image vector-function (mapcat identity keys)))))
