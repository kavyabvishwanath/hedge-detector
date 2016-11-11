import sys

from nltk import tokenize

# from nltk.stem import WordNetLemmatizer

import csv

import codecs, locale



sys.stdout = codecs.getwriter('utf8')(sys.stdout)

sys.stderr = codecs.getwriter('utf8')(sys.stderr)



#{hedge:type}

dictionary = {}

#{hedge_start:[[hedge,type,[tokenized]]]}

multiword_dictionary = {}

# wordnet_lemmatizer = WordNetLemmatizer()


def check_hedge_pos(token, pos):
    if token == 'like':
        return pos[0] != 'v'
    if token == 'about':
        return pos[0] != 'i'
    if token == 'might' or token == 'may':
        return pos[0] != 'n'
    """
    # Seth's additions
    if token == 'around':
        return pos == 'in'
    if token == 'kinda':
        return pos[:2] == 'rb'
    if token == 'likely':
        return pos[:2] == 'rb' # this could be weird - not sure about "That is likely to happen."
    if token == 'may':
        return pos[0] != 'n' # not sufficient - just excludes month
    if token == 'might':
        return pos[0] != 'n' # not sufficient - just excludes might==strength
    if token == 'pretty':
        return pos[0] != 'j'
    """

    return True
    
def check_hedge_next(lemma, next):
    """
    if lemma == 'suppose':
        return next != 'to'
    if lemma == 'find':
        return next != 'out'
    """
    return True

def check_hedge_deps(lemma, begin_ind, dependencies):
    """
    if lemma == 'about':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if dependent_ind == begin_ind:
                if relation == 'quantmod':
                    return True
        return False
    """
    if lemma == 'appear' or lemma == 'assume' or lemma == 'consider':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'xcomp' or relation == 'ccomp':
                    return True
        return False
    if lemma == 'believe':
        neg = False # if 'believe' is negated
        aux = False # if 'believe' in this sentence has a modal as a dependent (excluding 'do')
        sub_clause = False
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'aux' and dependent != 'do':
                    aux = True
                elif relation == 'neg':
                    neg = True
                elif relation == 'ccomp' or relation == 'xcomp':
                    sub_clause = True
        # 'believe' is only a hedge if: it has a subordinate clause, and isn't used with a negated modal
        return sub_clause and not (aux and neg)
    if lemma == 'doubt':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'neg':
                    return False
                elif relation == 'case' and (dependent == 'beyond' or dependent == 'without'):
                    return False
        return True
    if lemma == 'fairly':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if dependent_ind == begin_ind:
                if relation == 'advmod' and head_pos[0] == 'v':
                    return False
        return True
    if lemma == 'find':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if dependent == 'out':
                    return False
                if relation == 'dobj': # this is a little questionable - maybe do lower confidence instead of just 'no'
                    return False
        return True
    if lemma == 'general':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'case' and dependent == 'in':
                    return True
                elif head_pos[0] == 'n':
                    return False
            # more cases
        return True
    if lemma == 'impression':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'poss':
                    return True
                elif relation == 'case' and dependent == 'under':
                    return True
            elif dependent_ind == begin_ind:
                if relation == 'dobj' and (head == 'get' or head == 'have'):
                    # dobj(have, impression) is questionable - eg "I have a good Dylan impression"/"She had a profound impression on me"
                    # Might be better to look for dobj(impression, x) + ccomp(x, y) here - maybe specifically w/ 'that' as complementizer
                    return True
        return False
    if lemma == 'kinda' or lemma == 'likely' or lemma == 'unlikely':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                # 'kinda' should only be a hedge if it's an adverb, but the pos tagger tends to get it wrong, so this might be a better approach
                if dependent_pos[0] == 'N':
                    return False
        return True
    if lemma == 'know':
        neg = False # if 'know' is negated
        advcl = False # if 'know' is modified by an adverbial clause, usually headed by 'if'
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            # This is possibly excluding sentences like "I don't know that we should go", but those seem less common
            if head_ind == begin_ind:
                if relation == 'neg':
                    neg = True
                elif relation == 'advcl':
                    advcl = True
        return neg and advcl
    if lemma == 'like':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'cop' and dependent == 'be':
                    return True
            elif dependent_ind == begin_ind:
                if relation == 'mark':
                    return False
        return True
    if lemma == 'often':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'advmod' and (dependent == 'more' or dependent == 'less' or dependent == 'as'):
                    return False
        return True
    if lemma == 'partial':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'nsubj':
                    return False
        return True
    if lemma == 'pretty':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            # The following checks are essentially proxies for 'is pretty an adjective' - POS tagging usually fails that though
            if head_ind == begin_ind:
                if relation == 'cop':
                    return False
            elif dependent_ind == begin_ind:
                if head_pos[0] == 'n' and (relation == 'advmod' or relation == 'amod'):
                    # Stanford CoreNLP is not aware that adverbs cannot modify nouns
                    return False
        return True
    if lemma == 'rather':
        rather_verbs = set() # verbs modified by rather
        advmod_verbs = set() # verbs modified by adverbs other than rather
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                # "Rather than" is never a hedge
                if relation == 'mwe' and dependent == 'than':
                    return False
            elif dependent_ind == begin_ind:
                if relation == 'advmod':
                    # rather modifying an adjective is always a hedge ("His behavior is rather strange")
                    if head_pos[0] == 'j':
                        return True
                    if head_pos[0] == 'v':
                        rather_verbs.add(head_ind)
            elif relation == 'advmod': # look for dependencies of verbs modified by non-rather adverbs
                if head_pos[0] == 'v':
                    advmod_verbs.add(head_ind)
        # If rather modifies a verb in the dependencies, it is only a hedge if there is also an adverb modifying
        # that verb. ("She's acting rather strangely" -> advmod(act, rather) + advmod(act, strangely) = hedge,
        # vs "She'd rather go to the store" -> advmod(go, rather) = not hedge)
        return len(rather_verbs & advmod_verbs) != 0
    if lemma == 'really':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                # This is incomplete (sometimes 'really' is still not a hedge when negated, but better than nothing
                if relation == 'neg':
                    return True
        return False
    if lemma == 'roughly':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if dependent_ind == begin_ind:
                if relation == 'advmod' and head_pos[0] == 'v':
                    return False
        return True
    if lemma == 'suppose':
        to_deps = set()
        supposed_deps = set()
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if relation == 'mark' and dependent == 'to':
                to_deps.add(head_ind)
            elif head_ind == begin_ind:
                supposed_deps.add(dependent_ind)
        # 'supposed' is not a hedge if used as 'supposed to', meaning the intersection of to_deps and supposed_deps == 0
        return len(to_deps & supposed_deps) == 0
    if lemma == 'sure':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'neg':
                    return True
        return False
    if lemma == 'tend':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'xcomp':
                    return True
        return False
    if lemma == 'think':
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if head_ind == begin_ind:
                if relation == 'prep_of' and (dependent_pos[0] == 'n' or dependent_pos == 'prp'):
                    return False
        return True
    if lemma == 'totally': # "necessarily" was implemented with the same logic but it hurt
        neg_deps = set() # negated tokens
        lemma_deps = set() # tokens modified by totally
        for relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos in dependencies:
            if relation == 'neg':
                if head_ind == begin_ind:
                    return True # This will probably never happen
                neg_deps.add(head_ind)
            elif dependent_ind == begin_ind:
                lemma_deps.add(head_ind)
        # Totally is only a hedge if the word it modifies is also negated (eg 'not totally true')
        return len(neg_deps & lemma_deps) != 0

    return True

def find_hedges(sentences, all_deps):
    #for sent in sentences:
    for s in range(len(sentences)):
        sent = sentences[s]
        sent_deps = all_deps[s]
        #if sent[len(sent) - 1] != '?':
        for i in range(len(sent)):
            token = sent[i][0].lower()
            pos = sent[i][1].lower()
            lemma = sent[i][2].lower()
            begin_ind = sent[i][3]
            # print lemma
            if token in multiword_dictionary:
                hedge = multiword_dictionary[token]
                for j in range(len(hedge)):
                    match = True
                    for k in range(len(hedge[j][2])):
                        if i + k >= len(sent) or sent[i + k][0].lower() != hedge[j][2][k] or sent[i+k][4] != '_':
                            match = False
                    if match:
                        for k in range(len(hedge[j][2])):
                            sent[i+k][4] = 'M' + str(k) + '\t1\t' + hedge[j][1]
            if lemma in dictionary and sent[i][4] == '_':
                pos_ok = check_hedge_pos(token, pos)
                deps_ok = check_hedge_deps(lemma, begin_ind, sent_deps)
                next_ok = True
                if i + 1 < len(sent):
                    next_ok = check_hedge_next(lemma, sent[i+1][0].lower())
                if pos_ok and next_ok and deps_ok:
                    if (isinstance(deps_ok, float)):
                        sent[i][4] = 'S\t' + str(deps_ok) + '\t' + dictionary[lemma]
                    else:
                        sent[i][4] = 'S\t1\t' + dictionary[lemma]

            
def read_dictionary():
    """
    with open('hedge/multiword.txt') as f:
        reader = f.readlines()
        for line in reader:
            line = line.strip()
            #[[hedge,type,def_type,def_hedge,ex_hedge,def_not,ex_not,stem,prob]]
            tokenized = tokenize.word_tokenize(line.lower())
            if tokenized[0] not in multiword_dictionary:
                multiword_dictionary[tokenized[0]] = []
            multiword_dictionary[tokenized[0]].append([line, 'multiword', tokenized])
    """
    with open('hedgeClassifier/multiword_dict.csv', 'rU') as f:
        reader = csv.DictReader(f, delimiter='\t')
        for row in reader:
            hedge = row['Hedge'].strip().lower()
            tokenized = tokenize.word_tokenize(hedge)
            if tokenized[0] not in multiword_dictionary:
                multiword_dictionary[tokenized[0]] = []
            multiword_dictionary[tokenized[0]].append([hedge, 'multiword', tokenized])
    """
    with open('hedge/hProp.txt') as f:
        reader = f.readlines()
        for line in reader[1:]:
            line = line.strip()
            #print line
            dictionary[line.lower()] = 'hProp'
    with open('hedge/hRel.txt') as f:
        reader = f.readlines()
        for line in reader[1:]:
            line = line.strip()
            dictionary[line.lower()] = 'hRel'
    """
    with open('hedgeClassifier/dictionary.csv', 'rU') as f:
        reader = csv.DictReader(f, delimiter='\t')
        for row in reader:
            dictionary[row['Hedge'].strip().lower()] = row['Type'].strip()

def read_sentences(tokens):
    sentences = []
    sentence = []
    for token in tokens:
        trimmed = token.strip()
        if len(trimmed) == 0:
            if len(sentence) > 0:
                sentences.append(sentence)
                sentence = []
        else:
            word, tag, lemma, begin_ind = trimmed.split('\t')
            #print word
            #print tag
            #lemma = wordnet_lemmatizer.lemmatize(word,tag[0].lower()) if tag[0].lower() in ['a','n','v'] else wordnet_lemmatizer.lemmatize(word)
            #print lemma
            sentence.append([word, tag, lemma, int(begin_ind), '_'])
    if len(sentence) > 0:
        sentences.append(sentence)
    return sentences

def read_dependencies(lines):
    all_deps = []
    sentence_deps = []
    for line in lines:
        trimmed = line.strip()
        if len(trimmed) == 0:
            if len(sentence_deps) > 0:
                all_deps.append(sentence_deps)
                sentence_deps = []
            continue
        if trimmed == 'NO DEPS':
            all_deps.append([])
            continue
        relation, head, dependent, head_ind, dependent_ind, head_pos, dependent_pos = trimmed.split('\t')
        sentence_deps.append([relation, head.lower(), dependent.lower(), int(head_ind), int(dependent_ind), 
            head_pos.lower(), dependent_pos.lower()])
    if len(sentence_deps) > 0:
        all_deps.append(sentence_deps)
    return all_deps


def print_tagged(sentences):
    for sentence in sentences:
        for token in sentence:
            print token[0] + '\t' + token[4]
        print

if __name__=="__main__":
    tokens_file = codecs.open(sys.argv[1],encoding='utf-8')
    tokens = tokens_file.readlines()
    sentences = read_sentences(tokens)

    deps_file = codecs.open(sys.argv[2],encoding='utf-8')
    all_deps = read_dependencies(deps_file.readlines())
    read_dictionary()

    find_hedges(sentences, all_deps)
    print_tagged(sentences)
    
