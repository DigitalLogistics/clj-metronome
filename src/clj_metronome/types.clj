(ns clj-metronome.types
  (:require [schema.core :as s]))

(def FynderClassType
  {:id s/Int
   :name s/Str
   :description s/Str
   :picture (s/maybe s/Str)})

(def FynderTrainer
  {:userId s/Int
   :picture (s/maybe s/Str)
   :email (s/maybe s/Str)
   :telephone (s/maybe s/Str)
   :name s/Str
   :description s/Str})

(def FynderLocation
  {:id s/Int
   :description s/Str
   :address s/Str
   :timeZone s/Str ;; TODO
   :name s/Str})

(def FynderClass
  {:id s/Int
   :start s/Str ;; TODO
   :end s/Str ;; TODO
   :location FynderLocation
   :attending (s/maybe s/Bool)
   :attendeeCount s/Int
   :capacity s/Int
   (s/optional-key :conflictingClasses) []
   :type FynderClassType
   :trainer (s/maybe FynderTrainer)})

(def ScheduleRequest
  {:businessId s/Int
   :location {:id s/Int}
   (s/optional-key :date) {:start s/Any
                           :end s/Any} ;;; TODO
   })
