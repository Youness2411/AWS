# #!/bin/sh

# API_URL="http://backend:8080/api"
# USERNAME="initadmin"
# EMAIL="initadmin@example.com"
# PASSWORD="initpassword"

# echo "🚀 Initialisation de 200 théories de test pour la pagination..."

# # Créer l'utilisateur admin
# curl -s -X POST "$API_URL/auth/register" -H "Content-Type: application/json" -d "{\"username\": \"$USERNAME\", \"email\": \"$EMAIL\", \"password\": \"$PASSWORD\", \"role\": \"ADMIN\"}" >/dev/null

# # Récupérer le token
# TOKEN=$(curl -s -X POST "$API_URL/auth/login" -H "Content-Type: application/json" -d "{\"email\": \"$EMAIL\", \"password\": \"$PASSWORD\"}" | grep -o '"token":"[^"]*"' | cut -d':' -f2 | tr -d '"')

# if [ -z "$TOKEN" ]; then
#   echo "❌ Erreur: Impossible de récupérer le token d'authentification"
#   exit 1
# fi

# echo "✅ Token d'authentification récupéré"

# export LC_ALL=C.UTF-8 LANG=C.UTF-8

# # Créer 200 théories de test
# echo "📝 Création de 200 théories de test..."

# SUCCESS_COUNT=0
# ERROR_COUNT=0

# for i in $(seq 1 200); do
#   # Générer des titres variés
#   case $((i % 10)) in
#     0) TITLE="Théorie One Piece #$i - Luffy et ses alliés" ;;
#     1) TITLE="Théorie One Piece #$i - Le One Piece et sa localisation" ;;
#     2) TITLE="Théorie One Piece #$i - Les D. et leur mystère" ;;
#     3) TITLE="Théorie One Piece #$i - L'Histoire Perdue" ;;
#     4) TITLE="Théorie One Piece #$i - Les Yonko et l'équilibre" ;;
#     5) TITLE="Théorie One Piece #$i - La Marine et la justice" ;;
#     6) TITLE="Théorie One Piece #$i - Les Fruits du Démon" ;;
#     7) TITLE="Théorie One Piece #$i - Le Nouveau Monde" ;;
#     8) TITLE="Théorie One Piece #$i - La Révolution" ;;
#     9) TITLE="Théorie One Piece #$i - La fin de l'histoire" ;;
#   esac

#   # Générer du contenu varié
#   CONTENT="Voici une théorie détaillée sur One Piece numéro $i. Cette théorie explore différents aspects de l'univers créé par Eiichiro Oda. Elle contient des analyses approfondies des personnages, de l'intrigue et des mystères qui entourent l'histoire de Monkey D. Luffy et de son équipage. Cette théorie numéro $i apporte une perspective unique sur l'univers de One Piece et propose des hypothèses intéressantes sur l'évolution future de l'histoire."

#   # Ajouter des variations au contenu
#   if [ $((i % 3)) -eq 0 ]; then
#     CONTENT="$CONTENT Cette théorie se concentre particulièrement sur les aspects politiques de l'univers One Piece."
#   elif [ $((i % 3)) -eq 1 ]; then
#     CONTENT="$CONTENT Cette théorie analyse en profondeur les capacités des Fruits du Démon."
#   else
#     CONTENT="$CONTENT Cette théorie explore les relations entre les différents personnages de l'histoire."
#   fi

#   # Marquer certaines théories comme liées au dernier chapitre
#   IS_LAST_CHAPTER="false"
#   if [ $((i % 7)) -eq 0 ]; then
#     IS_LAST_CHAPTER="true"
#   fi

#   CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_URL/theories/post" \
#     -H "Authorization: Bearer $TOKEN" \
#     -F "title=$TITLE" \
#     -F "content=$CONTENT" \
#     -F "isRelatedToLastChapter=$IS_LAST_CHAPTER")

#   if [ "$CODE" -eq 200 ] || [ "$CODE" -eq 201 ]; then
#     SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
#     if [ $((i % 20)) -eq 0 ]; then
#       echo "✅ Créé $i/200 théories..."
#     fi
#   else
#     ERROR_COUNT=$((ERROR_COUNT + 1))
#     echo "❌ Erreur création théorie #$i -> Code: $CODE"
#   fi
  
#   # Petit délai pour éviter de surcharger l'API
#   sleep 0.1
# done

# echo ""
# echo "🎉 Initialisation terminée !"
# echo "✅ Théories créées avec succès: $SUCCESS_COUNT"
# echo "❌ Erreurs: $ERROR_COUNT"
# echo "📊 Total: $((SUCCESS_COUNT + ERROR_COUNT))/200"

# if [ $SUCCESS_COUNT -gt 0 ]; then
#   echo ""
#   echo "🧪 Test de pagination:"
#   echo "   - Page 1 (10 éléments): http://localhost:8080/api/theories/all?page=0&size=10"
#   echo "   - Page 2 (10 éléments): http://localhost:8080/api/theories/all?page=1&size=10"
#   echo "   - Page 20 (10 éléments): http://localhost:8080/api/theories/all?page=19&size=10"
#   echo "   - Toutes les théories: http://localhost:8080/api/theories/all"
# fi


